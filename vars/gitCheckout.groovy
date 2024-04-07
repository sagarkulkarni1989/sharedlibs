def call(String repoUrl, String branch = 'main') {
    git branch: branch, url: repoUrl
}
