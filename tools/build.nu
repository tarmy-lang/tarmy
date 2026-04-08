def run-tarmy [] {
    javac (path join ".." "TarmyAPI" "api.java") (path join ".." "runtarm.java")

    let sep = if ($nu.os-info.family == 'windows') { ";" } else { ":" }
    let parent = (".." | path expand)
    let api = (path join ".." "TarmyAPI" | path expand)
    
    java -cp $"($parent)($sep)($api)" runtarm
}
