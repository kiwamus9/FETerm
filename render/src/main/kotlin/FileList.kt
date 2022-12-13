import dev.fritz2.core.RootStore
import kotlinx.browser.window
import kotlin.js.Date
import kotlin.math.pow
import kotlin.math.roundToLong

const val FILE_TYPE_CHECK_MASK = 0xF000

enum class FileType {
    S_IFSOCK {
        override val mask: Int = 0xC000
        override val typeName: String = "ソケット"
    },
    S_IFLNK {
        override val mask: Int = 0xA000
        override val typeName: String = "シンボリックリンク"
    },
    S_IFREG {
        override val mask: Int = 0x8000
        override val typeName: String = "ファイル"
    },
    S_IFBLK {
        override val mask: Int = 0x6000
        override val typeName: String = "ブロックデバイス"
    },
    S_IFDIR {
        override val mask: Int = 0x4000
        override val typeName: String = "ディレクトリ"
    },
    S_IFCHR {
        override val mask: Int = 0x2000
        override val typeName: String = "キャラクターデバイス"
    },
    S_IFIFO {
        override val mask: Int = 0x1000
        override val typeName: String = "FIFO"
    },
    ;

    abstract val mask: Int
    abstract val typeName: String
}

@Suppress("unused")
external interface Stat {
    val dirname: String
    val basename: String
    val extname: String
    val gid: Int
    val uid: Int
    val mode: Int
    val size: Long
    val nlink: Long
    val rdev: Long
    val dev: Long
    val ino: Long
    val blksize: Long
    val blocks: Long
    val atime: Date //アクセス日　ファイルがアクセスされた時間
    val atimeMs: Double
    val birthtime: Date //作成日　ファイルが作成された時間
    val birthtimeMs: Double
    val ctime: Date //追加日　inodeが変更された時間
    val ctimeMs: Double
    val mtime: Date //変更日　ファイルが変更された時間
    val mtimeMs: Double
}

data class FileItemData(
    val dirname: String, // ディレクトリ名
    val basename: String, // ファイル名
    val extname: String, // 拡張子名
    val groupname: String,
    val username: String,
    val type: FileType,
    val mode: Int,
    val size: Long,
    val atime: Date, //アクセス日　ファイルがアクセスされた時間
    val birthTime: Date, //作成日　ファイルが作成された時間
    val ctime: Date, //追加日　inodeが変更された時間
    val mtime: Date, //変更日　ファイルが変更された時間
    // ここから拡張プロパティ
    var isChildrenExpand: Boolean = false,
    var children: MutableList<FileItemData>?,

    ) {
    val isHidden: Boolean = basename.startsWith(".")
    val absoluteDirDepth: Int = MainProcPathPackage.dirDepthCalc(dirname)

    constructor(stat: Stat) : this(
        dirname = stat.dirname,
        basename = stat.basename,
        extname = stat.extname,
        groupname = mainProcUserIDPackage.groupname(stat.gid),
        username = mainProcUserIDPackage.username(stat.uid),
        // 777 radix 8 = 511 radix 10
        type = FileType.values().find {
            it.mask == (stat.mode and FILE_TYPE_CHECK_MASK)
        } ?: error("Undefined File Type"),
        mode = stat.mode,
        size = stat.size.toString().toLong(), // TODO: 型変換考えないと
        atime = stat.atime,
        birthTime = stat.birthtime,
        ctime = stat.ctime,
        mtime = stat.mtime,
        children = null,
        isChildrenExpand = false,
    )

    @Suppress("unused")
    constructor(fullPathName: String) : this(
        window.getRawFstatLocal(fullPathName)
    )

    fun getHumanizeString(className: String, lang: String): String {
        return when (className) {
            "FLCol_basename" -> fileNameHumanizer(basename)
            "FLCol_mtime" -> dateHumanizer(mtime)
            "FLCol_birthTime" -> dateHumanizer(birthTime)
            "FLCol_aTime" -> dateHumanizer(atime)
            "FLCol_cTime" -> dateHumanizer(ctime)
            "FLCol_size" -> fileSizeHumanizer(size, type)
            "FLCol_type" -> fileTypeHumanizer(extname, type)
            "FLCol_mode" -> mode.toString()
            "FLCol_username" -> username

            else -> ""
        }

    }
}

fun fileNameHumanizer(fileName: String): String {
    return fileName
}

fun fileSizeHumanizer(fileSize: Long, fileType: FileType): String {
    val exponentUnit = listOf<String>("Byte", "KB", "MB", "GB", "TB", "EB", "PB")
    val cardinalNumberBase = 1000.0

    //通常ファイルでなければ計算しない
    if (fileType != FileType.S_IFREG) return "--"

    val fileSizeDouble: Double = fileSize.toDouble()
    exponentUnit.forEachIndexed { index, unit ->
        val cardinalNumber: Double = cardinalNumberBase.pow(index)
        val maxNumber: Double = cardinalNumberBase.pow(index + 1)
        if (fileSizeDouble < maxNumber) {
            val fileSizeString = ((fileSizeDouble * 10.0 / cardinalNumber).roundToLong() / 10.0).toString()
            return if (index <= 1) {
                fileSizeString.split(".")[0] + " " + unit
            } else {
                "$fileSizeString $unit"
            }
        }
    }
    return "Error"
}

fun dateHumanizer(timeStamp: Date): String {
    val now = Date()
    val todayStart = Date(Date.parse("${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}T00:00:00+09:00"))
    val yesterdayStart = Date(todayStart.getTime() - 24.0 * 60.0 * 60.0 * 1000.0)
    val beforeYesterdayStart = Date(todayStart.getTime() - 2.0 * 24.0 * 60.0 * 60.0 * 1000.0)

    fun int2digit(value: Int): String {
        return value.toString().padStart(2, '0')
    }

    return when (timeStamp.toLocaleDateString()) {
        todayStart.toLocaleDateString() ->
            "今日 ${int2digit(timeStamp.getHours())}:${int2digit(timeStamp.getMinutes())}"

        yesterdayStart.toLocaleDateString() ->
            "昨日 ${int2digit(timeStamp.getHours())}:${int2digit(timeStamp.getMinutes())}"

        beforeYesterdayStart.toLocaleDateString() -> "一昨日 ${timeStamp.getHours()}:${timeStamp.getMinutes()}"
        else ->
            "${timeStamp.getFullYear()}/${
                int2digit(timeStamp.getMonth() + 1)
            }/${
                int2digit(timeStamp.getDate())
            } ${
                int2digit(
                    timeStamp.getHours()
                )
            }:${
                int2digit(
                    timeStamp.getMinutes()
                )
            }"
    }
}

fun fileTypeHumanizer(extname: String, fileType: FileType): String {
    return if (fileType == FileType.S_IFREG) {
        when (extname.lowercase()) {
            ".png" -> "PNGイメージ"
            ".jpg" -> "JPEGイメージ"
            ".svg" -> "SVGイメージ"
            ".pdf" -> "PDF書類"
            else -> "ファイル"
        }
    } else fileType.typeName
}

fun getFileItemListLocal(fullPathDirName: String): List<FileItemData> {
    val rawList = window.getRawFstatListLocal(fullPathDirName)
    return rawList.map { FileItemData(it) }
}

object FileItemListStore : RootStore<List<FileItemData>>(listOf()) {
    private var fileItemDataList: FileItemDataList? = null
    val setup = handle<FileItemDataList> { _, newList ->
        fileItemDataList = newList
        newList.toFlatList()
    }
    val expand = handle<FileItemData> { oldList, item ->
        fileItemDataList?.run {
            expand(item)
            toFlatList()
        } ?: oldList
    }
    val shrink = handle<FileItemData> { oldList, item ->
        fileItemDataList?.run {
            shrink(item)
            toFlatList()
        } ?: oldList
    }
    val sort = handle<Pair<String, Boolean>> { oldList, sortCondition ->
        fileItemDataList?.run {
            sort(sortCondition.first, sortCondition.second)
            toFlatList()
        } ?: oldList
    }

}

class FileItemDataList(fullPathDirName: String) {
    private val innerList: MutableList<FileItemData> = mutableListOf()

    init {
        val rawList = window.getRawFstatListLocal(fullPathDirName)
        rawList.forEach { innerList.add(FileItemData(it)) }
    }

    fun expand(item: FileItemData) {
        if (item.type == FileType.S_IFDIR) {
            item.isChildrenExpand = true
            item.children = getFileItemListLocal(item.dirname + "/" + item.basename).toMutableList()
        }
    }

    fun shrink(item: FileItemData) {
        if (item.type == FileType.S_IFDIR) {
            item.isChildrenExpand = false
            item.children = null
        }
    }

    fun toFlatList(): List<FileItemData> {
        fun flat(itemDataList: MutableList<FileItemData>): MutableList<FileItemData> {
            val result = mutableListOf<FileItemData>()
            for (itemData in itemDataList) {
                result.add(itemData)
                if (itemData.isChildrenExpand) result.addAll(flat(itemData.children!!))
            }
            return result
        }
        return flat(innerList)
    }

    fun sort(className: String, toBigger: Boolean) {
        fun sortInner(className: String, targetList: MutableList<FileItemData>, toBigger: Boolean) {
            when (className) {
                "FLCol_basename" ->
                    if (toBigger) targetList.sortBy { it.basename } else targetList.sortByDescending { it.basename }

                "FLCol_mtime" ->
                    if (toBigger) targetList.sortBy { it.mtime.getMilliseconds() }
                    else targetList.sortByDescending { it.mtime.getMilliseconds() }

                "FLCol_birthTime" ->
                    if (toBigger) targetList.sortBy { it.birthTime.getMilliseconds() }
                    else targetList.sortByDescending { it.birthTime.getMilliseconds() }

                "FLCol_aTime" ->
                    if (toBigger) targetList.sortBy { it.atime.getMilliseconds() }
                    else targetList.sortByDescending { it.atime.getMilliseconds() }

                "FLCol_cTime" ->
                    if (toBigger) targetList.sortBy { it.ctime.getMilliseconds() }
                    else targetList.sortByDescending { it.ctime.getMilliseconds() }

                "FLCol_size" ->
                    if (toBigger) targetList.sortBy { it.size } else targetList.sortByDescending { it.size }

                "FLCol_type" ->
                    if (toBigger) targetList.sortBy { it.type } else targetList.sortByDescending { it.type }

                "FLCol_mode" ->
                    if (toBigger) targetList.sortBy { it.mode } else targetList.sortByDescending { it.mode }

                "FLCol_username" ->
                    if (toBigger) targetList.sortBy { it.username } else targetList.sortByDescending { it.username }
            }
            targetList.forEach {
                if (it.isChildrenExpand && (it.children !== null))
                    sortInner(className, it.children!!, toBigger)
            }
        }
        sortInner(className, innerList, toBigger)
    }
}


