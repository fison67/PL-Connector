/**
 *  Plex News (v.0.0.1)
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

metadata {
	definition (name: "Plex News", namespace: "fison67", author: "fison67") {
        capability "Sensor"
        
        attribute "typeName", "string"
        attribute "movieTitle", "string"
        attribute "moveiGenre", "string"
        attribute "movieSummary", "string"
        
        attribute "lastCheckin", "Date"
	}

	simulator { }
    
    preferences { }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def updateData(data){
	log.debug data
    if(data.typeName){
    	sendEvent(name: "typeName", value: data.typeName)
    }
    if(data.type == "movie"){
        if(data.title){
            sendEvent(name: "movieTitle", value: data.title)
        }
        if(data.summary){
            sendEvent(name: "movieSummary", value: data.summary)
        }
        if(data.genre){
            sendEvent(name: "moveiGenre", value: data.genre.join(", "))
        }
    }
    updateLastTime()
}

def updateLastTime(){
	def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed: false)
}

def updated() {}
