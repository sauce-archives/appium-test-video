#!groovy

pipeline {
    agent docker:"maven:3.3.9"

    stages {
        stage("test") {
            steps {
                sh "mvn test"
            }
        }
    }

    post {
        always {
            archive "*.mp4"
            junit "target/surefire-reports/*.xml"
        }
        failure {
            slackSend channel: "#dev", color: "bad", message: "Appium video test failed against ${APPIUM_SERVER} - ${API_BASE_URL} (<${BUILD_URL}|open>)", teamDomain: "testobject", token: "***REMOVED***"
        }
    }
}
