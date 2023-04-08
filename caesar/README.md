# Caesar and Affine Cipher
This program allows you to encrypt and decrypt messages using the Caesar and Affine cipher.

## Usage
You can run the script using Scala CLI with the following command:

`scala-cli run src/main/scala/caesar.scala -- {-a | -c} {-e | -d | -j | -k}`

where:  
- **-c** (Caesar cipher)
- **-a** (Affine cipher)
- **-e** (encryption)
- **-d** (decryption)
- **-j** (cryptanalysis with known plaintext)
- **-k** (cryptanalysis based solely on ciphertext)

The program reads input from specific files and writes output to other predetermined files. The filenames are fixed and cannot be changed. The following files are used:

- **plain.txt**: file with the plaintext
- **crypto.txt**: file with the ciphertext
- **decrypt.txt**: file to save the decrypted plaintext
- **key.txt**: file containing the key (one line with two numbers separated by a space, the first number representing the shift for the Caesar cipher or the second coefficient for the Affine cipher, and the second number representing the first coefficient for the Affine cipher)
- **key-new.txt**: file to save the recovered key
- **extra.txt**: file with additional plaintext in case of cryptanalysis with known plaintext.
## Functionality
### Encryption
The encryption program reads the plaintext and the key, and saves the encrypted text to **crypto.txt**. If the key is incorrect, an error message is displayed.

### Decryption
The decryption program reads the ciphertext and the key, and saves the decrypted text to **decrypt.txt**. If the key is incorrect, an error message is displayed.

### Cryptanalysis with Known Plaintext
The cryptanalysis program with known plaintext reads the ciphertext and the additional plaintext, and saves the calculated key and decrypted text to **key-new.txt** and **decrypt.txt**, respectively. If it is impossible to calculate the key, an error message is displayed.

### Cryptanalysis Based Solely on Ciphertext
The cryptanalysis program based solely on ciphertext reads the ciphertext and saves all possible candidate plaintexts to **decrypt.txt**. For the Caesar cipher, there are 25 possible candidates, and for the Affine cipher, there are 312 possible candidates.

### Error Handling
The program should not require the existence of unnecessary files for a given option. If the output files do not exist, they should be created. If the key is incorrect or impossible to calculate, an error message should be displayed.
