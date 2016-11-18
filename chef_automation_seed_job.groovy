def cookbooks = ["jenkins-liatrio","nexus-liatrio","archiva-liatrio","apache2-liatrio","hygieia-liatrio","tomcat-liatrio","selenium-liatrio","sonarqube-liatrio"]

cookbooks.each {
    cookBookName->

    def repoUrl = "https://github.com/liatrio-chef/" + cookBookName + ".git"
    def unitTestJobName = "chef-cookbook-" + cookBookName + "-1-unit-test"
    def testKitchenJobName = "chef-cookbook-" + cookBookName + "-2-test-kitchen"
    def knifeUploadJobName = "chef-cookbook-" + cookBookName + "-3-knife-upload"

    job(unitTestJobName){
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
        }
    }

    job(testKitchenJobName){
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
        }
    }

    job(knifeUploadJobName){
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
    }

}
