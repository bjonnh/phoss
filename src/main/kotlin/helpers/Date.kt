import java.text.SimpleDateFormat
import java.util.*

fun getDate(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(Calendar.getInstance().time)