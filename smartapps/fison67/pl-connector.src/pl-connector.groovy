/**
 *  PL Connector (v.0.0.1)
 *
 * MIT License
 *
 * Copyright (c) 2019 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
 
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field

definition(
    name: "PL Connector",
    namespace: "fison67",
    author: "fison67",
    description: "A Connector between Plex and ST",
    category: "My Apps",
    iconUrl: "https://mpng.pngfly.com/20180715/ccz/kisspng-roku-plex-television-computer-icons-hollywood-pictures-home-entertainment-5b4c169dd012c3.5323738215317131818523.jpg",
    iconX2Url: "https://mpng.pngfly.com/20180715/ccz/kisspng-roku-plex-television-computer-icons-hollywood-pictures-home-entertainment-5b4c169dd012c3.5323738215317131818523.jpg",
    iconX3Url: "https://mpng.pngfly.com/20180715/ccz/kisspng-roku-plex-television-computer-icons-hollywood-pictures-home-entertainment-5b4c169dd012c3.5323738215317131818523.jpg",
    oauth: true
)

preferences {
   page(name: "mainPage")
}


def mainPage() {
	 dynamicPage(name: "mainPage", title: "Plex Connector", nextPage: null, uninstall: true, install: true) {
   		section("Request New Devices"){
        	input "address", "string", title: "Server address", required: true, description:"IP:Port. ex)192.168.0.100:30200"
        }
        
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    
    if (!state.accessToken) {
        createAccessToken()
    }
    
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    initialize()
}

def initialize() {
	log.debug "initialize"
    
    def options = [
     	"method": "POST",
        "path": "/settings/api/smartthings",
        "headers": [
        	"HOST": settings.address,
            "Content-Type": "application/json"
        ],
        "body":[
            "app_url":"${apiServerUrl}/api/smartapps/installations/",
            "app_id":app.id,
            "access_token":state.accessToken
        ]
    ]
    
    def myhubAction = new hubitat.device.HubAction(options, null, [callback: null])
    sendHubCommand(myhubAction)
    
    if(!existChild("pl-connector-news")){
        try{
            def childDevice = addChildDevice("fison67", "Plex News", "pl-connector-news", location.hubs[0].id, [
                "label": "Plex News"
            ])    
        }catch(err){
            log.error err
        }
    }
}

def _getServerURL(){
     return settings.address
}

def existChild(dni){
	def result = false
	def list = getChildDevices()
    list.each { child ->
        if(child.getDeviceNetworkId() == dni){
        	result = true
        }
    }
    return result
}

def addDevice(){
	def result = []
    def data = request.JSON
    def id = data.id
    if(!existChild("pl-connector-${id}")){
        try{
            def childDevice = addChildDevice("fison67", "Plex Device", "pl-connector-${id}", location.hubs[0].id, [
                "label": "Plex - ${data.name}"
            ])    
            result = ["result":true, "reason": "success"]
        }catch(err){
            log.error err
            result = ["result":false, "reason": "error"]
        }
    }else{
        result = ["result":true, "reason": "exist"]
    }
    def resultString = new groovy.json.JsonOutput().toJson(result)
    render contentType: "application/javascript", data: resultString
}

def updateDevice(){
	log.debug "updateDevice"
    def data = request.JSON
    log.debug data
    def dni = "pl-connector-" + data.id
    def chlid = getChildDevice(dni)
    if(chlid){
		chlid.updateData(data.data)
    }
    def resultString = new groovy.json.JsonOutput().toJson("result":true)
    render contentType: "application/javascript", data: resultString
}

def getDeviceList(){
	def list = getChildDevices();
    def resultList = [];
    list.each { child ->
        def dni = child.deviceNetworkId
        resultList.push( dni.substring(13, dni.length()) );
    }
    
    def configString = new groovy.json.JsonOutput().toJson("list":resultList)
    render contentType: "application/javascript", data: configString
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "Plex Connector API",
        platforms: [
            [
                platform: "SmartThings Plex Connector",
                name: "Plex Connector",
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

def authError() {
    [error: "Permission denied"]
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/config")                         { action: [GET: "authError"] }
        path("/list")                         	{ action: [GET: "authError"]  }
        path("/update")                         { action: [POST: "authError"]  }
        path("/add")                         	{ action: [POST: "authError"]  }

    } else {
        path("/config")                         { action: [GET: "renderConfig"]  }
        path("/list")                         	{ action: [GET: "getDeviceList"]  }
        path("/update")                         { action: [POST: "updateDevice"]  }
        path("/add")                         	{ action: [POST: "addDevice"]  }
    }
}
