def getChannel() {
    return params.DEPLOY_CHANNEL != "" ? params.DEPLOY_CHANNEL : 'nightly'
}

def getBuildNumber() {
    return currentBuild.number
}

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(artifactDaysToKeepStr: "", artifactNumToKeepStr: "16", daysToKeepStr: "", numToKeepStr: "15"))
    }

    parameters {
        booleanParam(name: 'DEPLOY', defaultValue: true, description: 'Whether to deploy the result to the JetBrains marketplace')
        choice(name: 'DEPLOY_CHANNEL', choices: ['nightly', 'Stable'], description: 'Which channel do you want to deploy to?')
    }

    environment {
        JAVA_HOME = "/usr/lib/zulu8.33.0.1-jdk8.0.192-linux_x64"
        IJ_REPO_TOKEN = credentials('PHP_IJ_REPO_TOKEN')
        ORG_GRADLE_PROJECT_publishChannel = getChannel()
        ORG_GRADLE_PROJECT_buildNumber = getBuildNumber()
    }

    stages {
        stage ('validate') {
            steps {
                sh "./gradlew clean verifyPlugin"
            }
        }

        stage ('test') {
            steps {
                sh "./gradlew clean test"
            }
        }

        stage ('build') {
            steps {
                sh "./gradlew buildPlugin"

                archiveArtifacts artifacts: 'build/distributions/*.zip', fingerprint: true
            }
        }

        stage ('publish') {
            steps {
                sh "./gradlew publishPlugin"
            }
        }
    }
    post {
        cleanup {
            deleteDir()
        }
    }
}
