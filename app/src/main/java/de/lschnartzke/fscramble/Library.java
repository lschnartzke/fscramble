package de.lschnartzke.fscramble;


import de.lschnartzke.fscramble.cache.DataCache;
import de.lschnartzke.fscramble.scramblers.AbstractScrambler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a top-level interface to the same functionalities as the CLI interface.
 *
 * It provides a basic interface to:
 * - load the data directory
 * - create a set of files
 * - scramble a list of files or entire directories
 */
public class Library {
    /**
     * Simple method that prints some information about the library and exits.
     */
    public void check() {
        System.out.println("Hello, world");
    }

    /**
     * Initialize the data cache used during scrambling.
     * Currently only calls  <code>DataCache.Companion.init(path.toString())</code>
     *
     * @param path
     */
    public void loadDataDirectory(Path path) {
        DataCache.Companion.init(path.toString());
    }

    /**
     * Create `fileCount` files of type `extension`. The filenames will be randomly generated and the resulting files
     * will be stored in the provided `outpath`.
     * @param extension file type to generate (e.g. pdf, odt, txt, ...)
     * @param fileCount number of files to create
     * @param outpath directory where to store generated files
     * @param scrambleCount number of scramble actions to perform per file
     */
    public void createFiles(String extension, Path outpath, int scrambleCount, int fileCount) {
        if (!outpath.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("'outpath' must be a directory!"));

        AbstractScrambler scrambler = AbstractScrambler.Companion.getScramblerExtensionMap().get(extension);
        if (Objects.isNull(scrambler))
            throw new IllegalArgumentException(String.format("No scrambler for extension '%s' available", extension));

        while (fileCount-- > 0) {
            String filename = RandomStringUtils.secure().nextAlphanumeric(32) + "." + extension;

            try {
                // TODO: what do we do with the file object?
                // TODO: Maybe parallelize?
                File newFile = scrambler.createNewFile(filename, outpath.toString(), scrambleCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Scramble all files in the directory.
     *
     * @param inputDirectory directory to scramble
     * @param outputDirectory where to store scrambled files
     * @param scrambleCount how many scramble actions to perform per file.
     */
    public void scrambleDirectory(Path inputDirectory, Path outputDirectory, int scrambleCount) {
        if (!inputDirectory.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("'inputDirectory' must be a directory! (path: %s)", inputDirectory));

        if (!outputDirectory.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("'outputDirectory' must be a directory! (path: %s)", outputDirectory));

        // TODO: Currently, this does not recursively
        File[] inputDirectoryFiles = inputDirectory.toFile().listFiles();

        scrambleFiles(inputDirectoryFiles, outputDirectory, scrambleCount);
    }

    /**
     * Scramble the provided list of files.
     * @param files list of files to scramble
     * @param outputDirectory Where to store scrambled files
     * @param scrambleCount how many scramble actions to perform per file
     */
    public void scrambleFiles(File[] files, Path outputDirectory, int scrambleCount) {
        if (!outputDirectory.toFile().isDirectory())
            throw new IllegalArgumentException(String.format("'outputDirectory' must be a directory! (path: %s)", outputDirectory));

        // TODO: Parallelize

        Arrays.stream(files).forEach((file) -> {
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            AbstractScrambler scrambler = AbstractScrambler.Companion.getScramblerExtensionMap().get(extension);
            if (Objects.isNull(scrambler))
                return; // TODO: Maybe print an info?

            try {
                scrambler.scramble(file.getAbsolutePath(), outputDirectory.toString(), scrambleCount);
            } catch (Exception e) {
                // TODO: Log error
            }
        });
    }
}
