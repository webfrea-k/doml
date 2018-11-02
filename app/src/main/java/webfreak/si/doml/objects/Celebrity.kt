package webfreak.si.doml.objects

class Celebrity(private var name:String, private var daysLived: Int, private var image: String?) {

    fun getName(): String { return name  }
    fun getDaysLived(): Int { return daysLived }
    fun getAvatar(): String? { return image  }
}