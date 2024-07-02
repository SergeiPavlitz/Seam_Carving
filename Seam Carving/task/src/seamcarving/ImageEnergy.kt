package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

class ImageEnergy(private var image: BufferedImage) {
    private var energyMat = List(image.height) { MutableList(image.width) { 0.0 } }

    private var sumMat = List(image.height) { MutableList(image.width) { Double.MAX_VALUE } }

    private fun resetMats() {
        energyMat = List(image.height) { MutableList(image.width) { 0.0 } }
        sumMat = List(image.height) { MutableList(image.width) { Double.MAX_VALUE } }
    }

    fun resizeImage(widthCrop: Int, heightCrop: Int) {

        for (i in 0 until widthCrop) {
            getVerticallySeamedImage()
            resetMats()
        }

        for (i in 0 until heightCrop) {
            getHorizontallySeamedImage()
            resetMats()
        }

    }


    private fun getHorizontallySeamedImage() {
        countEnergy()

        for (i in 0 until image.height) {
            sumMat[i][0] = energyMat[i][0]
        }

        for (x in 0 until image.width - 1) {
            for (y in 0 until image.height) {

                val currSum = sumMat[y][x]

                setSumValue(currSum, x + 1, (y - 1).coerceIn(0, image.height - 1))
                setSumValue(currSum, x + 1, y)
                setSumValue(currSum, x + 1, (y + 1).coerceIn(0, image.height - 1))

            }
        }

        var minEnergyValue = Double.MAX_VALUE
        var minYInColumn = 0

        for (i in 0 until image.height) {
            val value = sumMat[i][image.width - 1]
            if (value < minEnergyValue) {
                minEnergyValue = value
                minYInColumn = i
            }
        }


        val seamPath = restorePathHor(minYInColumn)

        removeHorSeam(seamPath)

    }

    private fun removeHorSeam(seam: MutableList<Int>) {
        for (i in image.width - 1 downTo 0) {
            for (j in seam[i] until image.height - 1) {
                image.setRGB(i, j, image.getRGB(i, j + 1))
            }
        }
        image = image.getSubimage(0, 0, image.width, image.height - 1)
    }

    private fun restorePathHor(startY: Int): MutableList<Int> {
        val seamPath = mutableListOf<Int>()
        var y = startY
        var x = image.width - 1

        seamPath.add(y)

        while (x > 0) {
            x--
            val top = sumMat[(y - 1).coerceIn(0, image.height - 1)][x]
            val mid = sumMat[y][x]
            val bottom = sumMat[(y + 1).coerceIn(0, image.height - 1)][x]


            val list = listOf(top, mid, bottom).sorted()
            val min = list[0]

            val minY = when (min) {
                top -> {
                    y = (y - 1).coerceIn(0, image.height - 1)
                    y
                }

                mid -> {
                    y
                }

                else -> {
                    y = (y + 1).coerceIn(0, image.height - 1)
                    y
                }
            }

            seamPath.add(minY)

        }

        return seamPath.reversed().toMutableList()
    }


    private fun getVerticallySeamedImage() {
        countEnergy()

        for (i in 0 until image.width) {
            sumMat[0][i] = energyMat[0][i]
        }

        for (y in 0 until image.height - 1) {
            for (x in 0 until image.width) {
                val currSum = sumMat[y][x]

                setSumValue(currSum, (x - 1).coerceIn(0, image.width - 1), y + 1)
                setSumValue(currSum, x, y + 1)
                setSumValue(currSum, (x + 1).coerceIn(0, image.width - 1), y + 1)

            }
        }

        var minEnergyValue = Double.MAX_VALUE
        var minX = 0

        for (i in 0 until image.width) {
            val value = sumMat[image.height - 1][i]
            if (value < minEnergyValue) {
                minEnergyValue = value
                minX = i
            }
        }


        val seamPath = restorePathVert(minX)

        removeVertSeam(seamPath)
    }

    private fun removeVertSeam(seam: MutableList<Int>) {
        for (i in image.height - 1 downTo 0) {
            for (j in seam[i] until image.width - 1) {
                image.setRGB(j, i, image.getRGB(j + 1, i))
            }
        }
        image = image.getSubimage(0, 0, image.width - 1, image.height)
    }

    private fun restorePathVert(startX: Int): MutableList<Int> {
        val seamPath = mutableListOf<Int>()
        var y = image.height - 1
        var x = startX

        seamPath.add(x)

        while (y > 0) {
            y--
            val left = sumMat[y][(x - 1).coerceIn(0, image.width - 1)]
            val mid = sumMat[y][x]
            val right = sumMat[y][(x + 1).coerceIn(0, image.width - 1)]


            val list = listOf(left, mid, right).sorted()
            val min = list[0]

            val minX = when (min) {
                left -> {
                    x = (x - 1).coerceIn(0, image.width - 1)
                    x
                }

                mid -> {
                    x
                }

                else -> {
                    x = (x + 1).coerceIn(0, image.width - 1)
                    x
                }
            }


            seamPath.add(minX)

        }

//        if (prev > 0 && sum[i, res[i]] > sum[i, prev - 1]) res[i] = prev - 1;
//        if (prev < imgWidth - 1 && sum[i, res[i]] > sum[i, prev + 1]) res[i] = prev + 1;
        //https://habr.com/ru/articles/48518/

        return seamPath.reversed().toMutableList()
    }

    private fun setSumValue(curr: Double, x: Int, y: Int) {
        val energyValue = energyMat[y][x]
        val newSum = (curr + energyValue)
        if (newSum < sumMat[y][x]) {
            sumMat[y][x] = newSum
        }
    }

    private fun grad2(axis: String, x: Int, y: Int): Double {
        val pixel1 = Color(image.getRGB(if (axis == "x") x - 1 else x, if (axis == "y") y - 1 else y))
        val pixel2 = Color(image.getRGB(if (axis == "x") x + 1 else x, if (axis == "y") y + 1 else y))
        val rDiff = pixel2.red - pixel1.red
        val gDiff = pixel2.green - pixel1.green
        val bDiff = pixel2.blue - pixel1.blue
        return (rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble()
    }

    private fun energy(x: Int, y: Int): Double {
        return sqrt(
            grad2("x", x.coerceIn(1, image.width - 2), y) +
                    grad2("y", x, y.coerceIn(1, image.height - 2))
        )
    }

    private fun countEnergy() {
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {

                val energy = energy(x, y)

                energyMat[y][x] = energy
            }
        }
    }

    fun getImage(): BufferedImage {
        return image
    }

}