package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
//    println("INPUT ARGS")
//    println(args.joinToString())
//    val inputPath = "D:\\w_kotlin\\Seam Carving\\Seam Carving\\task\\test\\small.png"
//    val outputPath = "D:\\w_kotlin\\small-seam.png"
//    val inputPath = "D:\\w_kotlin\\Seam Carving\\Seam Carving\\task\\test\\blue.png"
//    val outputPath = "D:\\w_kotlin\\blue-resized.png"
//    val widthCrop = 125
//    val heightCrop = 50
    val inputPath = args[1]
    val outputPath = args[3]
    val widthCrop = args[5].toInt()
    val heightCrop = args[7].toInt()
    val inputImage = ImageIO.read(File(inputPath))

    val imageEnergy = ImageEnergy(inputImage)
    imageEnergy.resizeImage(widthCrop, heightCrop)

//    ImageIO.write(imageEnergy.getEnegrizedImage(), "png", File(outputPath))
//    ImageIO.write(imageEnergy.getVerticallySeamedImage(), "png", File(outputPath))
//    ImageIO.write(imageEnergy.getHorizontallySeamedImage(), "png", File(outputPath))
    ImageIO.write(imageEnergy.getImage(), "png", File(outputPath))

}


fun negativeImage(image: BufferedImage): BufferedImage {

    for (x in 0 until image.width) {               // For every column.
        for (y in 0 until image.height) {          // For every row
            val color = Color(image.getRGB(x, y))
            val colorNew = Color(255 - color.red, 255 - color.green, 255 - color.blue)
            image.setRGB(x, y, colorNew.rgb)  // Set the new color at the (x, y) position
        }
    }

    return image
}


fun drawBlackImage(w: Int, h: Int, type: Int = BufferedImage.TYPE_INT_RGB): BufferedImage {
    val image = BufferedImage(w, h, type)

    for (x in 0 until image.width) {               // For every column.
        for (y in 0 until image.height) {          // For every row
            val colorNew = Color.BLACK  // Create a new Color instance with the red value equal to 255
            image.setRGB(x, y, colorNew.rgb)  // Set the new color at the (x, y) position
        }
    }

    return image
}

/**
 * -1 is needed for the passing of hash test
 */
fun drawCross(image: BufferedImage) {
    val graphics = image.createGraphics()
    graphics.color = Color.RED
    graphics.drawLine(0, 0, image.width - 1, image.height - 1)
    graphics.drawLine(image.width - 1, 0, 0, image.height - 1)
}

