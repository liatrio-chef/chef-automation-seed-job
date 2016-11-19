def cookbooks = ["jenkins-liatrio","nexus-liatrio","archiva-liatrio","apache2-liatrio","hygieia-liatrio","tomcat-liatrio","selenium-liatrio","sonarqube-liatrio"]

cookbooks.each {
    cookBookName->

    def repoUrl = "https://github.com/liatrio-chef/" + cookBookName + ".git"
    def unitTestJobName = "chef-cookbook-" + cookBookName + "-1-unit-test"
    def testKitchenJobName = "chef-cookbook-" + cookBookName + "-2-test-kitchen"
    def knifeUploadJobName = "chef-cookbook-" + cookBookName + "-3-knife-upload"

    job(unitTestJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        scm {
            git{
                branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        triggers {
            scm('H/2 * * * *')
        }
        steps {
            shell(readFileFromWorkspace("unit_test.sh"))
        }
        publishers {
            downstream(testKitchenJobName, 'SUCCESS')
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(false)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('NONE')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
        }
    }

    job(testKitchenJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        scm {
            git{
                branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        steps {
            shell("kitchen test -d always --color")
        }
        publishers {
            downstream(knifeUploadJobName, 'SUCCESS')
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(false)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('NONE')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
        }
    }

    job(knifeUploadJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        scm {
            git{
                branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        steps {
            shell("#knife cookbook upload " + cookBookName)
        }
        publishers {
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(false)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('NONE')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
        }
    }

}
