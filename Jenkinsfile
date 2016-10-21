#!groovy

pipeline {
    agent docker:"maven:3.3.9"

    stages {
        stage("git") {
            sh "ls"
            checkout scm
        }

        stage("test") {
            "mvn test"
        }

        stage("collect artifacts") {
            archive "*.mp4"
            junit "target/surefire-reports/*.xml"
        }
    }

    notifications {
        failure {
            slackSend channel: "#dev", color: "bad", message: "Appium video test failed (<${BUILD_URL}|open>)", teamDomain: "testobject", token: "***REMOVED***"
        }
    }
}