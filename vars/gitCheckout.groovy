def call(String repoUrl, String branch = 'main') {
    stage('Git Checkout') {
        steps {
            script {
                git branch: branch, url: repoUrl
            }
        }
    }
}
