run_tarmy() {
    javac "../TarmyAPI/api.java" "../runtarm.java"

    local parent=$(realpath "..")
    local api=$(realpath "../TarmyAPI")
    
    java -cp ".:${parent}:${api}" runtarm
}
