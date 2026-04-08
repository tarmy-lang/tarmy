function Invoke-Tarmy {
    javac (Join-Path ".." "TarmyAPI" "api.java") (Join-Path ".." "runtarm.java")

    $sep = if ($IsWindows) { ";" } else { ":" }
    $parent = Resolve-Path ".."
    $api = Resolve-Path (Join-Path ".." "TarmyAPI")
    $cp = "$($parent)$($sep)$($api)"

    java -cp $cp runtarm
}
