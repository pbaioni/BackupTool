# BackupTool
A simple backup tool with some useful options in order to synchronize folders in a customizable way

Once the project has been cloned and built, you should use the backup tool through a command terminal (or a calling script) like this:

java -jar yourPathToJar/backupTool-1.0.jar [mode+options] [sourcePath] [destPath]

Modes: [mandatory -s || -r] 
-s: performs a backup for each subfolder of source into dest. Each folder in destPath will have a specific backup result file 
-r: performs a backup of the first level of source into dest. A unique backup result file will be stored into destPath

"Options: [optional] 
c: copy new files of the source into destination 
u: update in dest all the files existing in both source and dest if source has a more recent version 
a: archive (zip format) dest files no more existing in source 
d: delete dest files no more existing in source

Examples:

subfolder mode, all options: java -jar yourPathToJar/backupTool-1.0.jar -scuad path1 path2

subfolder mode, copy + update options: java -jar yourPathToJar/backupTool-1.0.jar -scu path1 path2

root mode, copy + delete options: java -jar yourPathToJar/backupTool-1.0.jar -rcd path1 path2
