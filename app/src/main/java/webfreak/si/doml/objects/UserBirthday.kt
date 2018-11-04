package webfreak.si.doml.objects
import kotlin.collections.HashMap

data class UserBirthday(
    var birthdays: HashMap<String, Long> = HashMap(),
    val id: String = "",
    var token: String = "")