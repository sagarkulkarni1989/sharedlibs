def call(String buildStatus = 'UNKNOWN') {
    // Default values
    buildStatus = buildStatus ?: 'UNKNOWN'
    
    // Jenkins environment variables
    def jenkinsUrl = env.JENKINS_URL ?: 'http://localhost:8080/'
    def jobName = env.JOB_NAME ?: 'UnknownJob'
    def buildNumber = env.BUILD_NUMBER ?: '0'
    def buildUrl = "${jenkinsUrl}job/${jobName}/${buildNumber}/"
    def buildTimeStamp = currentBuild.timeInMillis ?: System.currentTimeMillis()
    def workspace = env.WORKSPACE ?: 'N/A'
    
    // Azure Logic Apps or Power Automate workflow URL
    def workflowUrl = 'https://prod2-15.centralindia.logic.azure.com:443/workflows/42a93d8007d14e09835bf83be3abd987/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=DQWRnM4MUZZvTqzmORmkia2X1Q793By3ta1IKtPzLUQ'

    // JSON payload
    def jsonPayload = """
    {
        "message": "Build ${buildStatus} for job ${jobName}.",
        "title": "Jenkins Build Notification",
        "attachments": [
            {
                "contentType": "application/vnd.microsoft.card.adaptive",
                "content": {
                    "type": "AdaptiveCard",
                    "version": "1.0",
                    "body": [
                        {
                            "type": "TextBlock",
                            "text": "Job Name: ${jobName}",
                            "wrap": true
                        },
                        {
                            "type": "TextBlock",
                            "text": "Build Number: ${buildNumber}",
                            "wrap": true
                        },
                        {
                            "type": "TextBlock",
                            "text": "Build Status: ${buildStatus}",
                            "wrap": true
                        },
                        {
                            "type": "TextBlock",
                            "text": "Timestamp: ${new Date(buildTimeStamp).format('yyyy-MM-dd HH:mm:ss')}",
                            "wrap": true
                        },
                        {
                            "type": "TextBlock",
                            "text": "Workspace: ${workspace}",
                            "wrap": true
                        },
                        {
                            "type": "TextBlock",
                            "text": "The build ${buildStatus} for job [${jobName}](${buildUrl}).",
                            "wrap": true
                        }
                    ],
                    "actions": [
                        {
                            "type": "Action.OpenUrl",
                            "title": "View Build",
                            "url": "${buildUrl}"
                        }
                    ]
                }
            }
        ]
    }
    """

    // Send the request using curl in Linux
    sh """
        curl -X POST -H "Content-Type: application/json" -d '${jsonPayload}' '${workflowUrl}'
    """
}
