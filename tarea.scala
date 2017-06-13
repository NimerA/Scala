import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URL

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Base64

import java.lang.Object
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.awt.Color
import java.nio.charset.Charset

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer

object Main {
  def main(args: Array[String]): Unit = {
    
    var server = HttpServer.create(new InetSocketAddress(8080), 0)
    server.createContext("/ejercicio1", new ejercicio1())
    server.createContext("/ejercicio2", new ejercicio2())
    server.createContext("/ejercicio3", new ejercicio3())
    server.createContext("/ejercicio4", new ejercicio4())
    server.setExecutor(null)
    server.start()
  }
}

class ejercicio1() extends HttpHandler{
    override def handle(t: HttpExchange){
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var cont = 0
            var output = new ByteArrayOutputStream()
            var input = t.getRequestBody()
            var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
            val test = new String(response, Charset.forName("UTF-8"))
            val idk = test.split("\"")
            var origen = idk(3).replace(' ', '+')
            var destino = idk(7).replace(' ', '+')
            println(origen)
            println(destino)
            val request_url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origen + "&destination=" + destino + "&key=AIzaSyAzzrnc71pLvEvOdY322DQwwbUsFQZT7Vg"
            val url = new URL(request_url)
            val br = new BufferedReader(new InputStreamReader(url.openStream()))
            var maps: String = ""
            var temp: String = ""
            while(br.ready()){
                temp = br.readLine()
                println(temp)
                maps = maps + temp
            }
            var splitted = maps.split("\"steps\" \\: \\[|\\],               \"traffic_speed_entry\"")
            splitted = splitted(1).split("\"start_location\" \\: |\"end_location\" \\: |,                     \"html_instructions\"|,                     \"travel_mode\"")
            val buf = scala.collection.mutable.ListBuffer.empty[String]
            var c = 0
            while(c < splitted.size){
                if(c % 2 == 1){
                    buf += splitted(c)
                    println(splitted(c))
                }
                c = c + 1
            }
            var steps = buf.toList
            var json = ""
            c=3
            if(steps.length == 1)
                json += "{\"ruta\":["  + steps(1) + "]}"
            else if(steps.length == 2)
                json = "{\"ruta\":["  + steps(1) + ", " + steps(0) + "]}"
            else if(steps.length == 3)
                json = "{\"ruta\":["  + steps(1) + ", " + steps(0) + ", " + steps(2) + "]}"
            else{
                json = "{\"ruta\":[" + steps(1) + ", " + steps(0) + ", " + steps(2) + ", "
                while(c < steps.size ){
                    if(c % 2 == 0){
                        json = json + steps(c) + ", "
                    }
                    c = c + 1
                }
            }
            json = json.dropRight(2)
            json = json + "]}"
            println(json)
            response = json.getBytes(Charset.forName("UTF-8"))
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong)
            os.write(response)
            os.close()
        }
    }
}

class ejercicio2() extends HttpHandler{
    override def handle(t: HttpExchange){
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var cont = 0
            var output = new ByteArrayOutputStream()
            var input = t.getRequestBody()
            var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
            val test = new String(response, Charset.forName("UTF-8"))
            val idk = test.split("\"")
            var origen = idk(3).replace(' ', '+')
            var request_url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + origen + "&key=AIzaSyDlWabEzv6sC9AW1F_C1rc_nOz9o2nm0Bg"
            var url = new URL(request_url)
            var br = new BufferedReader(new InputStreamReader(url.openStream()))
            var maps: String = ""
            var temp: String = ""
            while(br.ready()){
                temp = br.readLine()
                println(temp)
                maps = maps + temp
            }
            var splitted = maps.split("\"location\" \\: \\{|\\},            \"location_type\"")
            splitted = splitted(1).split("\"lat\" \\: |\"lng\" \\: |,| ")
            var lat = splitted(16)
            var lon = splitted(33)
            request_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lon + "&radius=500&type=restaurant&key=AIzaSyAp0wmWixdzDo3MBI7TIY1XN4okirXUeYM"
            url = new URL(request_url)
            br = new BufferedReader(new InputStreamReader(url.openStream()))
            var maps1 = ""
            var temp1 = ""
            while(br.ready()){
                temp1 = br.readLine()
                println(temp1)
                maps1 = maps1 + temp1
            }
            //println(maps1)
            splitted = maps1.split("\"location\" \\: \\{|}\\,            \"viewport\"|\"name\" \\:")
            //println(splitted(1) + "ESPACIO")
            val buf = scala.collection.mutable.ListBuffer.empty[String]
            var c = 0
            while(c < splitted.length){
                if(c % 3 == 1){
                    buf += splitted(c)
                }
                if(c % 3 == 0 && c != 0){
                    var idk = splitted(c).split("         \"") 
                    buf += idk(0)
                }
                //println(splitted(c) + "Espacio")
                c = c + 1
            }
            var steps = buf.toList
            steps.foreach(println)
            //println(steps(1))
            var json = ""
            c = 0
            json = json + "{\"restaurantes\":["
            while(c < steps.length/2){
                json = json + "{\"nombre\":" + steps(c * 2 + 1) + steps(c * 2) + "}, "
                c = c + 1
            }
            json = json.dropRight(2)
            json = json + "]}"
            println(json)
            response = json.getBytes(Charset.forName("UTF-8"))
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong)
            os.write(response)
            os.close()
        }
    }
}

class ejercicio3() extends HttpHandler{
    override def handle(t: HttpExchange){
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var cont = 0
            var output = new ByteArrayOutputStream()
            var input = t.getRequestBody()
            var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
            val test = new String(response, Charset.forName("UTF-8"))
            var idk = test.split("\"")
            val nombre = idk(3)
            var img_data = idk(7)
            var gray_img = ""
            var img = Base64.getDecoder().decode(img_data)
            var bais: ByteArrayInputStream = new ByteArrayInputStream(img)
            var editable_img: BufferedImage = ImageIO.read(bais)
            println(editable_img.getHeight())
            println(editable_img.getWidth())

            for(x <- 0 to editable_img.getWidth() - 1){
                for(y <- 0 to editable_img.getHeight() - 1){
                    var rgb = editable_img.getRGB(x, y)
                    var r = (rgb >> 16) & 0xFF
                    var g = (rgb >> 8) & 0xFF
                    var b = (rgb & 0xFF)
                    //println("old")
                    //println(rgb)

                    var grayLevel = (0.21 * r + 0.72 * g + 0.07 * b).toInt
                    var gray = grayLevel << 16 | (grayLevel << 8) | grayLevel
                    //println("new")
                    //println(gray)
                    editable_img.setRGB(x, y, gray)
                }
            }

            var baos: ByteArrayOutputStream = new ByteArrayOutputStream()
            ImageIO.write(editable_img, "bmp", baos)
            var new_img = baos.toByteArray()
            gray_img = Base64.getEncoder().encodeToString(new_img)
            
            var json = ""
            var name = nombre.split("\\.")
            json = "{\"nombre\":\"" + name(0) + "(blanco y negro)." + name(1) + "\", \"data\": \"" + gray_img + "\"}"
            response = json.getBytes(Charset.forName("UTF-8"))
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong)
            os.write(response)
            os.close()
        }
    }
}

class ejercicio4() extends HttpHandler{
    override def handle(t: HttpExchange){
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var cont = 0
            var output = new ByteArrayOutputStream()
            var input = t.getRequestBody()
            var response: Array[Byte] = Stream.continually(input.read).takeWhile(_ != -1).map(_.toByte).toArray
            val test = new String(response, Charset.forName("UTF-8"))
            
            var idk = test.split("\"")
            val nombre = idk(3)
            var img_data = idk(7)
            var alto_temp = idk(12).split("\\: |\\,")
            var ancho_temp = idk(14).split("\\: |\n")
            var alto = alto_temp(1).toInt
            ancho_temp = ancho_temp(1).split(" ")
            var ancho= ancho_temp(0).subSequence(0, ancho_temp(0).length - 1).toString().toInt

            println(alto)
            println(ancho)

            var small_img = ""

            var img= Base64.getDecoder().decode(img_data)
            var bais: ByteArrayInputStream = new ByteArrayInputStream(img)
            var editable_img: BufferedImage = ImageIO.read(bais)
            var smaller_img: BufferedImage = new BufferedImage(ancho, alto, 1)
            
            var height = editable_img.getHeight()
            var width = editable_img.getWidth()
            
            var divX = width.toFloat/ancho.toFloat
            var divY = height.toFloat/alto.toFloat

            var resizedWidth = (width/divX).toInt
            var resizedHeight = (height/divY).toInt
            println("" + width + " / " + ancho + " = " + divX)
            println("" + height + " / " + alto + " = " + divY)

            for(x <- 0 to resizedWidth - 1){
                for(y <- 0 to resizedHeight - 1){
                    var pixel = editable_img.getRGB((x * divX).toInt, (y * divY).toInt)
                    smaller_img.setRGB(x, y, pixel)
                }
            }

            var baos: ByteArrayOutputStream = new ByteArrayOutputStream()
            ImageIO.write(smaller_img, "bmp", baos)
            var new_img = baos.toByteArray()
            small_img = Base64.getEncoder().encodeToString(new_img)

            var json= ""
            var name = nombre.split("\\.")
            json = "{\"nombre\":\"" + name(0) + "(reducido)." + name(1) + "\", \"data\": \"" + small_img + "\"}"
            println(json)
            response = json.getBytes(Charset.forName("UTF-8"))
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong)
            os.write(response)
            os.close()
        }
    }
}