/** Dlink Cam */


preferences
{
	input("username", "text", title:"Username", description: "Your Dlink Name:")
	input("password", "password", title:"Username", description: "Your Dlink Name:")
	input("URL", "text", title:"IP", description: "URL of Camera")
	input("port", "text", title:"POrt", description: "Port")
}

metadata {
  tiles {
    carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }
    
    standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "default", label: "", action: "Image Capture.take", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
    }
    
    standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
      state "take", label: "", action: "Image Capture.take", icon: "st.secondary.take", nextState:"taking"
    }
    
    standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
	state "off", label: 'Off', action: "switch.on", icon: "st.custom.buttons.rec", backgroundColor: "#ccffcc", nextState: "on"
	state "on", label: 'On', action: "switch.off", icon: "st.custom.buttons.rec", backgroundColor: "#EE0000", nextState: "off"
}

main "camera"
details(["cameraDetails","take","button"])
}
}

def parseCameraResponse(def response) {
  if(response.headers.'Content-Type'.contains("image/jpeg")) {
    def imageBytes = response.data
    if(imageBytes) {
      storeImage(getPictureName(), imageBytes)
    }
  }
}

private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  "image" + "_$pictureUuid" + ".jpg"
}

private take() {
  log.debug("Take a photo")
httpGet("http://${settings.username}:${settings.password}@${settings.ip}:${settings.port}/image/jpeg.cgi"){response -> 
log.debug("Image captured")
  parseCameraResponse(response)
  }
}

def motionCmd(int motion){
sendEvent(name: 'motion', value: motion)
httpPost("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/setSystemMotion", "ReplySuccessPage=motion.htm&ReplyErrorPage=motion.htm&MotionDetectionEnable=${motion}&MotionDetectionSensitivity=85&ConfigSystemMotion=Save") 
{response ->
def content = response.data
log.debug content
}
}

def sendCmd(int num){
httpGet("http://${settings.username}:${settings.password}@${settings.host}:${settings.port}/decoder_control.cgi?command=${num}") {response ->
def content = response.data
log.debug content
}
}

def on() {
log.debug "Executing motion detection"
motionCmd(1)
sendEvent(name: "motion", value: "on")
         }

def off() {
log.debug "Disabling motion detection"
motionCmd(0)
sendEvent(name: "motion", value: "off")
}

def setMotion(status) {
log.debug "Status:$status"
motionCmd(status) {
sendEvent(name:'motion', value: status)
}
}
