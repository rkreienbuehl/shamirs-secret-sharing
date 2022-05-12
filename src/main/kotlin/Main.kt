import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

fun main(args: Array<String>) {
    val s = 42
    val numberOfShares = 6
    val minShares = 5

    // Shares generieren
    val generated = generateShares(s, numberOfShares, minShares)

    // Zufällige Shares auswählen
    val shares = randomEntries(generated, minShares)

    // Secret zurückrechnen
    val secret = reconstructSecret(shares)
    println(secret)
}

/**
 * Nimmt einen Int, welcher das [secret] darstellt und teilt diesen in Anzahl [numberOfShares], wobei die mindestens
 * benötigte Anzahl [minShares] beachtet wird.
 * @return Gibt eine Map<Int, Int> zurück mit den berechneten shares als Value und der Position als Key
 */
fun generateShares(secret: Int, numberOfShares: Int, minShares: Int): Map<Int, Int> {
    if (numberOfShares <= 0 || minShares <= 0 || numberOfShares < minShares) {
        throw IllegalArgumentException()
    }

    val random = IntArray(minShares-1) { Random.nextInt(1, 100) }

    val shares = HashMap<Int, Int>()

    for (i in 1..numberOfShares) {
        var s = secret
        for (j in 1 until minShares) {
            s += random[j-1] * (i.toDouble().pow(j).toInt())
        }

        shares[i] = s
    }

    return shares
}


/**
 * Funktion zur Reproduktion des Secrets
 * Implementation basierend auf
 * https://en.wikibooks.org/wiki/Algorithm_Implementation/Mathematics/Polynomial_interpolation
 *
 * Nimmt eine Map<Int, Int> mit [shares] mit einer Anzahl an shares,
 * welche der Anzahl mindestens gebrauchter shares entspricht (mehr oder weniger shares führen zu falschen Ergebnissen)
 * @return Gibt das berechnete Secret zurück
 */
fun reconstructSecret(shares: Map<Int, Int>): Int {
    val poly = IntArray(shares.size) { 0 }
    val term = DoubleArray(shares.size)
    val keys = shares.keys.toIntArray()
    val values = shares.values.toIntArray()

    for (i in 0 until shares.size) {
        var prod = 1.0

        for (j in 0 until shares.size) {
            if (i != j) {
                prod *= (keys[i] - keys[j])
            }
        }

        prod = values[i] / prod

        term[0] = prod

        for (j in 0 until shares.size) {
            if (i != j) {
                for (k in shares.size-1 downTo  1) {
                    term[k] += term[k-1]
                    term[k-1] *= (-keys[j].toDouble())
                }
            }
        }

        for (j in 0 until shares.size) {
            poly[j] += term[j].roundToInt()
        }
    }

    return poly[0]
}

/**
 * Generiert eine neue Map<K, V> mit der Anzahl [amount] aus zufälligen Einträgen aus einer Liste [list]
 * @return Gibt die generierte Map zurück
 */
fun <K, V> randomEntries(list: Map<K, V>, amount: Int): Map<K, V> {
    val result = HashMap<K, V>()
    val entries = list.toList()

    while (result.size != amount) {
        val entry = entries.random()

        if (!result.keys.contains(entry.first)) {
            result[entry.first] = entry.second
        }
    }

    return result
}