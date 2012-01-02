package grails.plugins.facebooksdk.scope

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class FacebookAppScope {
	
	def appId = 0
	
	GrailsWebRequest getRequest() {
		return RequestContextHolder.getRequestAttributes()
	}
	
	private String getKeyVariableName(String key) {
		assert this.appId, "Facebook appId must be defined"
		return "fb_${this.appId}_${key}"
	}
	
}
