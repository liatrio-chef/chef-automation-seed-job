// folder('chef')
// job("chef/" + unitTestJobName)
// add views via dsl and not config xml

def cookbooks = ["jenkins-liatrio","nexus-liatrio","archiva-liatrio","apache2-liatrio","hygieia-liatrio","tomcat-liatrio","selenium-liatrio","sonarqube-liatrio"]

cookbooks.each {
    cookBookName->

    def repoUrl = "https://github.com/liatrio-chef/" + cookBookName + ".git"
    def projectUrl = "https://github.com/liatrio-chef/" + cookBookName
    def unitTestJobName = "chef-cookbook-" + cookBookName + "-1-unit-test"
    def testKitchenJobName = "chef-cookbook-" + cookBookName + "-2-test-kitchen"
    def knifeUploadJobName = "chef-cookbook-" + cookBookName + "-3-knife-upload"

    job(unitTestJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        properties {
            githubProjectUrl(projectUrl)
        }
        scm {
            git{
                // branch("master")
                remote {
                    url(repoUrl)
                }
            }
        }
        triggers {
            // scm('H/2 * * * *') // use github hook to triiger
            githubPush()
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
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(true)
                includeTestSummary(false)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
            githubCommitNotifier()
        }
    }

    job(testKitchenJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
        }
        properties {
            githubProjectUrl(projectUrl)
        }
        scm {
            git{
                // branch("master")
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
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
            githubCommitNotifier()
        }
    }

    job(knifeUploadJobName){
        description("This job was created with automation.  Manual edits to this job are discouraged.")
        wrappers {
            colorizeOutput()
            credentialsBinding {
                file('chef-uploader', 'chef-uploader.pem')
            }
        }
        properties {
            githubProjectUrl(projectUrl)
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
            shell("""knife spork check ${cookBookName}

if [ `git log --pretty=oneline | head -1 | grep ''#major'` ]
then
  knife spork bump ${cookBookName}

elif [ `git log --pretty=oneline | head -1 | grep '#minor'` ]
then
  knife spork bump ${cookBookName}

elif [ `git log --pretty=oneline | head -1 | grep '#patch'` ]
then
  knife spork bump ${cookBookName}

else
  knife spork bump ${cookBookName}
fi

knife spork upload ${cookBookName}

#knife spork promote sandbox ${cookBookName} --remote""")
        }
        publishers {
            slackNotifier {
                notifyFailure(true)
                notifySuccess(true)
                notifyAborted(false)
                notifyNotBuilt(false)
                notifyUnstable(false)
                notifyBackToNormal(true)
                notifyRepeatedFailure(false)
                startNotification(false)
                includeTestSummary(true)
                includeCustomMessage(false)
                customMessage(null)
                buildServerUrl(null)
                sendAs(null)
                commitInfoChoice('AUTHORS_AND_TITLES')
                teamDomain(null)
                authToken(null)
                room('jenkins-build')
            }
            mailer('drew@liatrio.com', true, true)
            githubCommitNotifier()
        }
    }

}
