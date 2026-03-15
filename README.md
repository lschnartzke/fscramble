> [!WARNING]
> This README hasn't been updated in a while and is unbelievably out of date. Do not rely on anything you read except maybe the the first two sections. Anything covering 'how-to-use' is most likely false.

# `fscramble` - File Scrambler
`fscramble` is a *simple* tool that takes a directory of files and randomly scrambles their contents, writing the
result to a different directory. Its main purpose is to be used to rapidly simulate change in data over a long(er)
period of time. Alternatively it can be used to create a collection of randomized files.

# Core concepts
This section aims to provide a simple overview on how `fscramble` works. It explains how the scrambling takes place 
how parameters will affect the result.

## Data directory/Scrambling data
To provide somewhat authentic results, `fscramble` relies on text and media files that provide input for the 
scrambling process. Thus, at startup, the `--data-directory` is read into memory (note that this can cause issues
when there's too much content) and pre-processed as far as possible. During the scrambling or creation process the 
processed data items are then used to create the actual file content.

### Text files
Text files are read in paragraphs, thus any non-empty sequence separated by two consecutive newlines ('\n') can be found
in processed files. If the paragraphs contain nothing but whitespace characters, they are ignored.

### Media files
Media files, at the moment only Jpeg, PNG, Tiff and Gif files, are supported. The files are read during process
startup and stored in a list. They will be randomly put into documents that support embedding of media files
(such as PDFs or office documents).

## Scrambling process
During the scrambling process files are read from the `--input-directory`, modified in memory, and written out to the 
`--target-directory`. If the `--target-directory` does not exist, it will be created. The same applies to all
subdirectories inside `--input-directory`. Files inside `--input-directory` are never modified (unless `--input-directory`
== `--target-directory`, which is discouraged and hereby declared undefined behavior). Any errors when writing 
files will be transparently reported and the file will be skipped. 

## File creation
When creating files, the `--target-directory` will be used to store files and in case the directory does not exist,
it will be created. Note however, that there will be no subfolder structure created, thus all files will be 
stored directly under the `--target-directory`. 

# Usage
`fscramble` can be used in two different modes: plain CLI-parameters or using a configuration file.
## Command Line Usage
`fscramble` currently provides three modes of operation:
* `scramble` - read files from one directory, scramble them, and write the result to the target directory
* `create` - Create random files from scratch, either by specifying the total size all created files should occupy
* `run` - load a configuration file that specifies run-configuration for the `scramble` and `create` commands and run the specified one

### `scramble`
Usage: `fscramble scramble <options>`, where options can be any of the following:
* (required) `--input-directory <dir>`: Directory from which to read files
* (required) `--target-directory <dir>`: Directory to which scrambled files will be written
* (required) `--data-directory <dir>`: Directory which contains the content used to scramble files
* (optional) `--archives`: Also scramble (and create) archives
* (optional) `--archive-types <types...>`: What types of archives to create/scramble
* (optional) `--count <num>`: Amount of times a *scramble action* is performed per file. (Default: 20)
* (optional) `--create`: If present, alongside scrambling new files will also be created
* (optional) `--create-min <num>`: The minimum amount of files to create. Ignored if `--create` is not present (Default: 0)
* (optional) `--create-max <num>`: The maximum amount of files to create. Ignored if `--create` is not present (Default: 0)

  The scramble command will read files and subdirectories of `--input-directory` and scramble the files, recreating the directory structure under `--target-directory`. To provide somewhat authentic data, the files in the `--data-directory` are used during scrambling. The data directory itself will be left untouched.

### 'create'
Usage: `fscramble create <options>`, where options can be any of the following:
* (required) `--target-directory <dir>`: Directory to which created files will be written
* (required) `--data-directory <dir>`: Directory which will be used for the content of the created files
* (optional) `--input-directory <dir>`: Directory to use as input for archives. If omitted, `--data-directory` is used instead.
* (optional) `--archives`: Also create archives
* (optional) `--archive-types`: What types of archives to create
* (conditional) `--count <num>`: The amount of files to create. If present, a flat number of files will be created.
Conflicts with `--size`.
* (conditional) `--size <num>`: Create files until the accumulated file size is larger than or equal to the specified value.
Conflicts with `--count`. Note that the total size will almost always be larger than specified.
* (optional) `--file-types <types>`: What files (specified by extension) to create. By default, all available file types
are created. You can get a list of supported file types by running `fscramble --list-file-types`
* (optional) `--jobs <num>`: The amount of jobs to run in parallel. Will default to the amount of available processors.
Affects the precision of `--size`.

Some important information:
    * One of `--size` or `--count` is required. However, specifying both is not allowed.
    * When using `--size`, the provided value is to be treated as a lower bound. Due to the nature of file formats it is almost impossible to determine the size of the file once encoded and written to disk. The delta between actual size and `--size` is further amplified by the amount of `--jobs` to be run in parallel. The more jobs are run in parallel - the higher the deviation will be. This is to be expected.
    * `--count` does not respect the value of `--jobs`. At program startup, a job will be executed per file to create and be processed by the runtime. It is in theory possible to run into memory limits due to jobs requiring too much memory. In that case, there are two possible solutions: (1) Get more RAM (2) Use `--size` instead. If you find a valid reason for using `--count` please let me know and I'll consider fixing this purely hypothetical issue.

* `run` - Use a configuration file that specifies what the program does.
    * (required) `--config-file <file>`: The configuration file to load.
    * (optional) `--list`: If present, list all available modes in the provided `--configuration-file`. If present, `<mode>` is not required.
    * (required) `<mode>`: A mode present in the `--config-file` which should be run.
The configuration is written in yaml. In general, the following structure is expected:
* (top-level) `run`:
  * `<mode>` name of the mode
    * Mode configuration.

For example:
```yaml
run:
  create-fixed:
    command: "create"
    target-directory: "/tmp/out"
    data-directory: "./data"
    file-types:
      - pdf
      - txt
      - docx
    count: 32
  create-size:
    command: "create"
    target-directory: "/tmp/out"
    data-directory: "./data"
    size: 1G
  create-archives:
    command: "create"
    target-directory: "/tmp/out"
    data-directory: "./data"
    input-directory: "./input"
    archives: true
    archive-types:
      - tar
      - zip
```
Note that, in the configuration format, `size` can be used with units to simplify writing larger values. For clarification:
* Units `k, K, m, M, g, G, t, T` are supported
* The numeric value *must* be an integer.
* The numeric value is multiplied with `1024`.
    * (top-level) `run`:
      * `<mode>` name of the mode
        * Mode configuration.
  For example:
  ```yaml
  run:
    create-fixed:
      command: "create"
      target-directory: "/tmp/out"
      data-directory: "./data"
      file-types:
        - pdf
        - txt
        - docx
      count: 32
    create-size:
      command: "create"
      target-directory: "/tmp/out"
      data-directory: "./data"
      size: 1G
    create-archives:
      command: "create"
      target-directory: "/tmp/out"
      data-directory: "./data"
      input-directory: "./input"
      archives: true
      archive-types:
        - tar
        - zip
  ```
  Note that, in the configuration format, `size` can be used with units to simplify writing larger values. For clarification:
  * Units `k, K, m, M, g, G, t, T` are supported
  * The numeric value *must* be an integer.
  * The numeric value is multiplied with `1024`.

