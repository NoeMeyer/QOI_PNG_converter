package cs107;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bundles the project files that used to be uploaded to the EPFL grading
 * server into a local ZIP archive.
 *
 * The original version of this class POSTed the archive to
 * "https://cs108.epfl.ch/api_cs107/submissions", which is no longer
 * reachable. This offline version performs the exact same file collection
 * and zipping, but simply writes the archive to disk instead of sending it
 * over the network, so the project no longer depends on any server.
 */
public final class Submit {
    // CONFIGURATION
    // -------------
    // Fichiers additionnels à rendre, p.ex. "Main.java" (laisser vide pour un rendu normal)
    private static final String[] ADDITIONAL_FILES = {};
    // Nom du fichier ZIP généré localement
    private static final String OUTPUT_ZIP_NAME = "QOI_submission.zip";
    // -------------

    // NE MODIFIEZ RIEN EN DESSOUS DE CETTE LIGNE
    // DO NOT CHANGE ANYTHING BELOW THIS LINE

    private static final String[] TO_SUBMIT =
            {"ArrayUtils.java", "QOIDecoder.java", "QOIEncoder.java"};
    private static final String ZIP_ENTRY_NAME_PREFIX = "QOI/";

    public static void main(String[] args) {
        var moreAdditionalFiles = args.length >= 1
                ? Arrays.copyOfRange(args, 0, args.length)
                : new String[0];

        try {
            var projectRoot = Path.of(System.getProperty("user.dir"));
            var toSubmit = Stream.concat(
                            Stream.concat(Stream.of(TO_SUBMIT), Stream.of(ADDITIONAL_FILES)),
                            Stream.of(moreAdditionalFiles))
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
            var paths = filesToSubmit(
                    projectRoot,
                    p -> toSubmit.contains(p.getFileName().toString().toLowerCase()));

            if (paths.isEmpty()) {
                System.err.println("Erreur : aucun fichier à archiver n'a été trouvé sous " + projectRoot);
                System.exit(1);
            }

            var zipArchive = createZipArchive(paths);
            var outputPath = projectRoot.resolve(OUTPUT_ZIP_NAME);
            writeZip(outputPath, zipArchive);

            System.out.printf("""
                Votre archive a bien été générée localement sous le nom :
                  %s
                Elle est composée des fichiers suivants :
                  %s
                Aucun serveur n'a été contacté : l'archive est simplement enregistrée sur le disque.%n""",
                    outputPath,
                    paths.stream().map(Object::toString).collect(Collectors.joining("\n  ")));

            System.exit(0);
        } catch (IOException e) {
            System.err.println("Erreur inattendue !");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static List<Path> filesToSubmit(Path projectRoot, Predicate<Path> keepFile) throws IOException {
        try (var paths = Files.walk(projectRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(projectRoot::relativize)
                    .filter(keepFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    private static byte[] createZipArchive(List<Path> paths) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (var zipStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (var path : paths) {
                var entryPath = IntStream.range(0, path.getNameCount())
                        .mapToObj(path::getName)
                        .map(Path::toString)
                        .collect(Collectors.joining("/", ZIP_ENTRY_NAME_PREFIX, ""));
                zipStream.putNextEntry(new ZipEntry(entryPath));
                try (var fileStream = new FileInputStream(path.toFile())) {
                    fileStream.transferTo(zipStream);
                }
                zipStream.closeEntry();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void writeZip(Path filePath, byte[] zipArchive) throws IOException {
        try (var c = new FileOutputStream(filePath.toFile())) {
            c.write(zipArchive);
        }
    }
}
