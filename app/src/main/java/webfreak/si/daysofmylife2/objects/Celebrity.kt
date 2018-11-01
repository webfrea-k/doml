package webfreak.si.daysofmylife2.objects

class Celebrity(private var name:String, private var daysLived: Int, private var image: Int) {

    fun getName(): String { return name  }
    fun getDaysLived(): Int { return daysLived }
    fun getAvatar(): Int { return image  }
}