# Many-time pad
Originally, one-time pad is uncrackable because the key can be used only once. This program, called xor, demonstrates that using the key more than once enables the cipher to be broken.

## Usage
To use the program, run the following command in the command line:

`scala-cli run src/main/scala/xor.scala -- {-p | -e | -k}`

where:
- **-p**: prepares the text for the encryption (splits text into lines of length 64 and removes non-lowercase and non-space characters)
- **-e**: encrypts separately every line of the plain text using xor with a provided key
- **-k**: performs a cryptoanalysis on the given ciphertext

The program reads input from specific files and writes output to other predetermined files. The filenames are fixed and cannot be changed. The following files are used:
- **orig.txt**: a file containing any text
- **plain.txt**: a file containing at least several lines of equal length, e.g. 64 characters
- **key.txt**: a file containing a key, which is a string of any characters of the same length as the lines in **plain.txt**
- **crypto.txt**: a file containing the ciphertext, each line of which is the result of XOR operation with the key
- **decrypt.txt**: a file where the result of the cryptanalysis will be written
