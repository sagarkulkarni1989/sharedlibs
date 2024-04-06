def call(String repoUrl, String branch = 'main') {
    pipeline {
        agent any
        stages {
            stage('Git Checkout') {
                steps {
                    script {
                        git branch: branch, url: repoUrl
                    }
                }
            }
        }
    }
}
