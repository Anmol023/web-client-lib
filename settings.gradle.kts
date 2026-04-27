rootProject.name = "web-client-lib"

plugins {
    id("com.gradleup.nmcp.settings").version("1.4.4")
}


nmcpSettings {
    centralPortal {

        username = providers.gradleProperty("ossrhUsername").getOrElse("")
        password = providers.gradleProperty("ossrhPassword").getOrElse("")
        publishingType = "AUTOMATIC"
    }
}