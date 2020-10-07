package uk.ac.ebi.fgpt.zooma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class that is capable of backing up a {@link Path} to a backup location, usually in order to replace the original
 * path with a newer copy.  This class is intended for subclassing and is abstract with protected methods for that
 * reason.
 *
 * @author Tony Burdett
 * @date 16/12/15
 */
public abstract class ZOOMA2BackingUpDriver {
    protected void makeBackup(File f, PrintStream out) throws IOException {
        // make datestamped backup of 'f'
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String backupFileName = f.getName().concat(".backup.").concat(dateStr);
        File backupFile = new File(f.getAbsoluteFile().getParentFile(), backupFileName);

        Path path = f.toPath();
        Path backupPath = backupFile.toPath();

        backupFiles(path, backupPath, out);
    }

    protected void backupFiles(Path path, Path backupPath, PrintStream out) throws IOException {
        if (!Files.exists(backupPath)) {
            out.print(
                    "Backing up " + path.toString() + " to " + backupPath.toString() + "...");
            Files.copy(path,
                       backupPath,
                       StandardCopyOption.REPLACE_EXISTING);
            out.println("ok!");
        }
        else {
            out.print(
                    "Backup already exists for today, clearing " + path.toString() + "...");
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        out.println("ok!");
    }
}
