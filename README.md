# QOI ⇄ PNG Converter

A Java implementation of the **QOI** (Quite OK Image) format encoder and decoder,
originally the CS-107 mini-project at EPFL. This version runs **fully offline** 

## Features

- Encode PNG images to the QOI format (`pngToQoi`)
- Decode QOI files back to PNG (`qoiToPng`)
- Batch mode: `Main` converts **every** image in the `references/` folder in both
  directions and writes the results to `res/`
- A built-in test suite (run with assertions enabled) validating the array,
  encoder and decoder utilities

## How to run

From the project root:

```bash
javac -d out/production src/cs107/*.java
java -ea -cp out/production cs107.Main
```

Or open the project in IntelliJ IDEA and run the `cs107.Main` class
(make sure the working directory is the project root so `references/` and
`res/` resolve correctly).

`Main` runs the test suite, then converts every `.png` in `references/` to `.qoi`
and every `.qoi` to `.png`, saving everything into `res/`.

## Project structure

```
src/cs107/
  ArrayUtils.java        Low-level byte/array helpers
  QOISpecification.java  Format constants (tags, header, hash function)
  QOIEncoder.java        PNG -> QOI encoding
  QOIDecoder.java        QOI -> PNG decoding
  Helper.java            Image and binary file I/O (provided)
  Main.java              Entry point + batch conversion
  Submit.java            Bundles the source files into a local ZIP archive
  ...
references/              Sample PNG and QOI images used as input
res/                     Output folder (generated at runtime)
```

## Notes

The `Submit.java` helper originally uploaded the project to the EPFL grading
server. Since that endpoint is no longer available, it now simply writes a
`QOI_submission.zip` archive locally instead of contacting any server.
