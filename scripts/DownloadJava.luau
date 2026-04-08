local process = require("@lune/process")

local function openUrl(url)
    local os = process.os
    local command

    if os == "windows" then
        command = "start"
    elseif os == "macos" then
        command = "open"
    else -- Linux / Unix
        command = "xdg-open"
    end

    process.spawn(command, { url }, { shell = true })
end

-- Open the Java Download page
openUrl("https://www.java.com/en/download/")