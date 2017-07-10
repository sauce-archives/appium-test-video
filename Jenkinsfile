#!groovy

def runTest() {
    node {
        stage("checkout") {
            checkout scm
        }
        stage("test") {
            docker.image("maven:3.3.9").inside {
                try {
                    sh "mvn -B clean test"
                } finally {
                    junit "target/surefire-reports/*.xml"
                    archive "*.mp4"
                }
            }
        }
    }
}

if (env.APPIUM_SERVER.contains("staging.testobject.org")) {
    lock (resource: params.TESTOBJECT_DEVICE_ID) {
        runTest()
    }
} else {
    try {
        runTest()
        if (env.SUCCESS_NOTIFICATION_ENABLED) {
            slackSend channel: "#${env.SLACK_CHANNEL}", color: "good", message: "`${env.JOB_BASE_NAME}` passed (<${BUILD_URL}|open>)", teamDomain: "${env.SLACK_SUBDOMAIN}", token: "${env.SLACK_TOKEN}"
        }
    } catch (err) {
        if (env.APPIUM_SERVER.contains("testobject.com") || env.FAILURE_NOTIFICATION_ENABLED) {
            slackSend channel: "#${env.SLACK_CHANNEL}", color: "bad", message: "`${env.JOB_BASE_NAME}` failed: $err (<${BUILD_URL}|open>)", teamDomain: "${env.SLACK_SUBDOMAIN}", token: "${env.SLACK_TOKEN}"
        }
        throw err
    }
}
