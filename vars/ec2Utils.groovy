// vars/ec2Utils.groovy
def fetchEc2InstancesDetails() {
    script {
        // Ensure AWS CLI is installed
        sh 'aws --version'

        // Fetch EC2 instances details using AWS CLI
        sh '''
            aws ec2 describe-instances \
            --filters "Name=tag:Name,Values=test1" \
            --query "Reservations[*].Instances[*].{ID:InstanceId,Type:InstanceType,State:State.Name,Name:Tags[?Key=='Name']|[0].Value}" \
            --output table --region $AWS_REGION
        '''
    }
}

def checkAndStartStoppedInstances() {
    script {
        // Check instance status and start if stopped
        def instanceIds = sh(script: '''
            aws ec2 describe-instances \
            --filters "Name=tag:Name,Values=test1" "Name=instance-state-name,Values=stopped" \
            --query "Reservations[*].Instances[*].InstanceId" \
            --output text --region $AWS_REGION
        ''', returnStdout: true).trim()

        if (instanceIds) {
            echo "Found stopped instances: ${instanceIds}"
            sh """
            aws ec2 start-instances --instance-ids ${instanceIds} --region $AWS_REGION
            echo "Started instances: ${instanceIds}"
            """
            
            // Wait for instances to reach "running" state
            timeout(time: 5, unit: 'MINUTES') {
                waitUntil {
                    def allRunning = sh(script: """
                        aws ec2 describe-instances \
                        --instance-ids ${instanceIds} \
                        --query "Reservations[*].Instances[*].State.Name" \
                        --output text --region $AWS_REGION
                    """, returnStdout: true).trim().split().every { it == 'running' }
                    return allRunning
                }
            }

            // Wait for status checks to pass
            timeout(time: 10, unit: 'MINUTES') {
                waitUntil {
                    def allChecksPassed = sh(script: """
                        aws ec2 describe-instance-status \
                        --instance-ids ${instanceIds} \
                        --query "InstanceStatuses[*].InstanceStatus.Status" \
                        --output text --region $AWS_REGION
                    """, returnStdout: true).trim().split().every { it == 'ok' }
                    return allChecksPassed
                }
            }

            echo "All instances are running and passed status checks: ${instanceIds}"
        } else {
            echo "No stopped instances found with the tag Name=test1"
        }
    }
}
