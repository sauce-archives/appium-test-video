#!groovy

pipeline {
    agent {
        docker "maven:3.3.9"
    }

    stages {
        stage("test") {
            steps {
                lock (label: 'Motorola_Moto_E_2nd_gen_real') {
                    sh "mvn -B clean test"
                }
            }
        }
    }

    post {
        always {
            archive "*.mp4"
            junit "target/surefire-reports/*.xml"
        }
        failure {
            slackSend channel: "#${env.SLACK_CHANNEL}", color: "bad",message: "Appium video test failed against ${APPIUM_SERVER} - ${API_BASE_URL} (<${BUILD_URL}|open>)", teamDomain: "${env.SLACK_SUBDOMAIN}", token: "${env.SLACK_TOKEN}"
        }
    }
}
