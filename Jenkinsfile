pipeline {
    agent any

    stages {
        stage("Deploy docker image") {
            when {
                buildingTag()
            }

            steps {
                withGradle {
                    withCredentials([usernamePassword(
                        credentialsId: '5e8ae1b1-eead-4cda-8314-add11af4e44f',
                        usernameVariable: 'REDGAMES_REGISTRY_USERNAME',
                        passwordVariable: 'REDGAMES_REGISTRY_PASSWORD'
                    )]) {
                        gradlew("dockerPushImage")
                    }
                }
            }
        }
    }
}

def gradlew(String... args) {
    sh "./gradlew ${args.join(' ')} -s"
}
