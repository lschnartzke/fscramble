# fscramble
fscramble is a simple tool that can be used to randomly modify commonly used file types, such 
as office documents, spreadsheets and PDF files.

To do this, it requires a source of data to use for scrambling, which will be preloaded into memory,
and one or more input and outputu files (or directories)

## Scramble data
Scramble data is loaded during startup and randomly inserted during runtime.
It resides in the `data-directory`.

The `data-directory` should only contain files, currently the following types are supported (somewhat):
* png
* jpeg, jpg
* txt

(yes that's all of them)


## Input/Output - Scrambling
`fscramble` requires a list of files (or directories) to do its work. Thus, you need to pass either
files or directories to process as well as where to store the result.

For example, to scramble a single file:

```
java -jar fscramble --input in.pdf --output out.pdf --data-directory data
```

input and output MUST have the same number of arguments. (though currently only 1 arg for both is supported)

Input and output may either be pointing to files or directories, though:
* Output may be a directory, even when input is a file. The result will be stored at `<output>/<infile>`.
* Input and Output may both be directories. In that case, every file in the input directory will 
be scrambled and the result will be stored in the output directory, using the same filename
* Input and output may both be files (trivial)

## Scramble count
The `--scramble-count` flag defines how often the files should be scrambled. It is the amount of
applied action *per file*. Note that, in case of (handled) errors, the total number of modifications
may be lower, as the process simply continues instead of retrying.

Using a scramble count lower than 0 is hereby defined as undefined behavior (though likely does nothing)
