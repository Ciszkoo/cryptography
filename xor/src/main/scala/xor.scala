import scala.io.Source
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File
import scala.util.Try
import scala.util.Failure
import scala.util.Success

@main
def xor(arg: String): Unit = arg match
  case "-p" =>
    val origText: OriginalText = OriginalText.read()
    val plainText: PlainText   = origText.toPlain()
    plainText.write()
  case "-e" =>
    val plainText     = PlainText.read()
    val key           = Key.read()
    val encryptedText = plainText.encrypt(key)
    encryptedText.write()
  case "-k" =>
    val encryptedText = EncryptedText.read()
    val decryptedText = encryptedText.cryptanalysis()
    decryptedText.write()
  case _ => println("You chose wrong option")

trait Writable(text: String, filename: String) {

  def write(): Unit =
    val bw = new BufferedWriter(new FileWriter(new File(filename)))
    bw.write(text)
    bw.close()
}

trait Readable[A] {
  protected def readFile(filename: String): String =
    Try(Source.fromFile(filename)) match
      case Failure(exception) =>
        println(s"$filename not found.")
        sys.exit(1)
      case Success(value) => value.mkString

  def read(): A
}

class OriginalText private (text: String) {

  def toPlain(): PlainText = PlainText(
    text
      .replace("\n", " ")
      .filter(c => c.isSpaceChar || c.isLetter)
      .trim()
      .replace("  ", " ")
      .toLowerCase()
      .grouped(64)
      .reduce((acc, line) => acc + "\n" + line)
  )
}

object OriginalText extends Readable[OriginalText] {
  private val filename = "orig.txt"

  def read(): OriginalText = OriginalText(readFile(filename))
}

class PlainText(text: String) extends Writable(text, PlainText.filename) {

  def encrypt(key: Key): EncryptedText =
    EncryptedText(
      text.split("\n").map(_.zipWithIndex.map(char => (char(0) ^ key.text(char(1))).toChar)).flatten.mkString
    )
}

object PlainText extends Readable[PlainText] {
  private val filename = "plain.txt"

  def read(): PlainText = PlainText(readFile(filename))
}

class Key private (val text: String)

object Key extends Readable[Key] {
  val filename = "key.txt"

  def read(): Key = Key(readFile(filename))
}

class EncryptedText(text: String) extends Writable(text, EncryptedText.filename) {

  def cryptanalysis(): DecryptedText =
    val key = buildKey(text.grouped(64).toList)
    DecryptedText(
      text
        .grouped(64)
        .map(_.zipWithIndex.map(char => if key(char(1)) == '?' then '?' else (char(0) ^ key(char(1))).toChar))
        .flatten
        .mkString
        .grouped(64)
        .reduce((acc, line) => acc + "\n" + line)
    )

  private def buildKey(encryptedText: List[String]): String =
    val keyProt = String.valueOf(Array.fill(64)('?'))
    def helper(encryptedText: List[String], key: String = keyProt): String = encryptedText match
      case head :: second :: third :: tail => helper(second :: third :: tail, keyBuilder(head, second, third, key))
      case _                               => key
    helper(encryptedText)

  private def keyBuilder(line1: String, line2: String, line3: String, key: String): String =
    def helper(line1: String, line2: String, line3: String, key: String, keyModified: String = ""): String =
      (line1, line2, line3, key) match
        case (l1, l2, l3, key)
            if (!key.isEmpty() && !l1.isEmpty() && !l2.isEmpty() && !l3
              .isEmpty()) && (key.head != '?' || l1.head == l2.head || l1.head == l3.head || l2.head == l3.head) =>
          helper(l1.tail, l2.tail, l3.tail, key.tail, keyModified.appended(key.head))
        case (l1, l2, l3, key) if l3.length() > 0 =>
          val c12 = (l1.head ^ l2.head).toChar
          val c13 = (l1.head ^ l3.head).toChar
          val c23 = (l2.head ^ l3.head).toChar
          if (c12.isLetterOrDigit) && (c13.isLetterOrDigit) then
            helper(l1.tail, l2.tail, l3.tail, key.tail, keyModified.appended((l1.head ^ ' ').toChar))
          else if (c12.isLetterOrDigit) && (c23.isLetterOrDigit) then
            helper(l1.tail, l2.tail, l3.tail, key.tail, keyModified.appended((l2.head ^ ' ').toChar))
          else if (c13.isLetterOrDigit) && (c23.isLetterOrDigit) then
            helper(l1.tail, l2.tail, l3.tail, key.tail, keyModified.appended((l3.head ^ ' ').toChar))
          else helper(l1.tail, l2.tail, l3.tail, key.tail, keyModified.appended(key.head))
        case _ => keyModified.appendedAll(key)
    helper(line1, line2, line3, key)
}

object EncryptedText extends Readable[EncryptedText] {
  val filename = "crypto.txt"

  def read(): EncryptedText = EncryptedText(readFile(filename))
}

class DecryptedText(text: String, filename: String = "decrypt.txt") extends Writable(text, filename)
