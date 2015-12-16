package uk.ac.ebi.fgpt.zooma;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A class that is capable of backing up a {@link Path} to a backup location, usually in order to replace the original
 * path with a newer copy.  This class is intended for subclassing and is abstract with protected methods for that
 * reason.
 *
 * @author Tony Burdett
 * @date 16/12/15
 */
public abstract class ZOOMA2BackingUpDriver {
    protected void backupFiles(Path file, Path backupFile, PrintStream out) throws IOException {
        if (!Files.exists(backupFile)) {
            out.print(
                    "Backing up " + file.toString() + " to " + backupFile.toString() + "...");
            Files.move(file,
                       backupFile,
                       StandardCopyOption.REPLACE_EXISTING,
                       StandardCopyOption.ATOMIC_MOVE);
            out.println("ok!");
        }
        else {
            out.print(
                    "Backup already exists for today, clearing " + file.toString() + "...");
            Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
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
}
