AMI_VERSION = 0
BUILD_TYPE="SBT"
SCALA_VERSION="2.12"
ASSEMBLY="-assembly"
SERIVCE="user"
AMI_VERSION=0

def runParallel = true
def buildStages

QA_REGIONS= ["us-west-2","us-east-1"]
PROD_REGIONS= ["ap-southeast-1","eu-west-2","us-west-2","us-east-1"]


stage('Parameter Check'){
    node{
        TAG = "${params.TAG}"
        AUTO_INCREMENT_AMI_VERSION = "${params.AUTO_INCREMENT_AMI_VERSION}"
        RUN_TASK = "${params.RUN_TASK}"

        try{
            if (params.SERVICE == "") {
                throw new Exception("You must select Service")
            }
            if (params.TAG == "") {
                throw new Exception("Enter the artifact version for the build in TAG")
            }
        }catch(e){
            currentBuild.result = "FAILURE"
    //             throw(e)
            println(e)
        }
    }
}

stage("Git CheckOut"){
    node{
        println "Git CheckOut Started"
        checkout(
                [
                        $class                           : 'GitSCM',
                        branches                         : [[name: 'master']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: 'github_mjkong_ssh', url: 'https://github.com/cobb-salad/salad-ms-user.git']]
                ]
        )
        println "Git CheckOut End"
    }
}

stage("Build Artifact") {
    input(message: "Want to build artifact?") 
    node{
        if (params.BUILD_TYPE == "SBT"){

            BUILD_DIR="target/scala-${SCALA_VERSION}"
            SBT_LAUNCHER="1.5"
            JVM_OPTION="-Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts"
            SBT_OPTION="-Dsbt.log.noformat=true"
            SBT_ACTION="clean assembly"

            sh "ls -la"
        }else{
            BUILD_DIR="build/libs"
            GRADLE_TASK="clean install"
        }

        ami_version = 1
        println "${AMI_VERSION}"
        println "${ami_version}"

    }
}
// stage ("Parallel Builds") {
//     parallel (
//         "stream1" : {
//             stage("stream1"){
//                 echo "stream1"
//             }
//             stage("stream1-1"){
//                 echo "stream1-1"
//             }
//         },
//         "stream2" : {
//             stage("stream2") {
//                 echo "stream2"
//             }
//             stage("stream2-1"){
//                 echo "stream2-1"
//             }
//         },
//         "stream3" : {
//             stage("stream3") {
//                 echo "stream3"
//             }
//             stage("stream3-1"){
//                 echo "stream3-1"
//             }
//         }
//     )
// }
stage("pararrel"){

    buildStages = prepareBuildStages()
    for (builds in buildStages) {
        parallel(builds)
    }
}


// parallel worker_1: {
//     stage("worker_1"){
//         node(){
//             sh """hostname ; pwd """
//             print "on worker_1"
//         }
//     }
// },  worker_2: {
//     stage("worker_2"){
//         node(){
//             sh """hostname ; pwd """
//             print "on worker_2"
//         }
//     }
// },  worker_3: {
//     stage("worker_3"){
//         node(){
//             sh """hostname ; pwd """
//             print "on worker_3"
//         }
//     }
// }

stage("Build AMI") {
    // input(message: "AMI exist!!, Want to auto increment AMI version?") 
    // DEPLOY_SERVERS = input message: '', parametrs: [[$class: 'ChoiceParameterDefinition', choices:'SERVER1,SERVER2\nSERVER1\nSERVER2', description: '', name: 'DEPLOY_SERVERS']]
    def deploy_region = input(message: "choice", parameters: [

         extendedChoice(
            defaultValue: 'ap-southeast-1,eu-west-2,us-west-2,us-east-1',
            description: 'Some description',
            multiSelectDelimiter: ',',
            name: 'deploy_region',
            quoteValue: false,
            saveJSONParameterToFile: false,
            type: 'PT_CHECKBOX',
            value:'ap-southeast-1,eu-west-2,us-west-2,us-east-1',
            visibleItemCount: 4
        )
    ])
    node{
        
        println "${deploy_region}"
        if(AMI_VERSION > 0){
            println "AMI_VERSION is greater than 0"
        }

        VV=10

        withEnv([
            "AMI_VERSION=${VV}",
            "AMI=test/version-${VV}"
        ]){
            sh '''
                echo $AMI_VERSION
                echo $AMI
            '''


            sh '''
                echo $AMI_VERSION
                echo $AMI
            '''
        }


        sh '''
            echo $AMI_VERION
        '''

    //         userInput = input(
    //             id: 'Proceed1', message: 'AMI_VERSION is ${env.AMI_VERSION} : Do you want to use this version?', parameters: [
    //             [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
    //          ])
    //
    //         println "${userInput}"
    }
}

stage("after build ami"){
    node{
        env.TESTVAL2="ttt"
        env.AMI_ID="ami-123456789"
        env.ENVIRONMENT="qa"
        ENVIRON = "${env.ENVIRONMENT}"

        sh """#!/bin/bash
            set -x
            touch test2
            echo '\"ami-test\"' > test2

            touch change.sh
            chmod +x change.sh
            ls -al

            echo "#!/bin/bash" > change.sh
            cat change.sh
            #echo "sed -i \'s/ami-[^\"]*/ami-123456789/g\' test2" >> change.sh
            echo "sed -i \'s/\\bami-[^\\"]*/$AMI_ID/g\' test2" >> change.sh
            #sed -i 's/[^\"]*/'"$TESTVAL2"'/g' test2
            ./change.sh
            set +x
        """
        test()
    }
}

// Create List of build stages to suit
def prepareBuildStages() {
  def buildStagesList = []

  for (i=1; i<5; i++) {
    def buildParallelMap = [:]
    for (name in [ 'one', 'two'] ) {
      def n = "${name} ${i}"
      buildParallelMap.put(n, prepareOneBuildStage(n))
    }
    buildStagesList.add(buildParallelMap)
  }
  return buildStagesList
}

def prepareOneBuildStage(String name) {
  return {
    stage("Build stage:${name}") {
        node{
            println("Building ${name}")
        }
    }
  }
}

def getASGINFO(jsonString){

    env.ASGINFO = "/var/lib/jenkins/jsontmp"

    sh '''#!/bin/bash
        jq '. | .MinSize,.MaxSize,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity,.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity' /var/lib/jenkins/jsontmp > asginfo.out
    '''

    ASGOUT = readFile("asginfo.out")
    def list = ASGOUT.readLines()

    return ASGOUT
}

def jsontest(){

    env.ASGINFO = "/var/lib/jenkins/jsontmp"
    ASGINFO=readFile("/var/lib/jenkins/jsontmp")
//     def object= new JsonSlurper().parseText(ASGINFO)
    def ASGINFOObj = jsonSlurper.parseText(ASGINFO)

    MINSIZE = ASGINFOObj.get('MinSize')
    MAXSIZE = ASGINFOObj.get('MaxSize')
    ONDEMANDCAPACITY = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandBaseCapacity')
    ONDEMANDRATIO = ASGINFOObj.get('MixedInstancesPolicy').get('InstancesDistribution').get('OnDemandPercentageAboveBaseCapacity')

//     MINSIZE = ASGINFOObj.MinSize
//     MAXSIZE = ASGINFOObj.MaxSize
//     ONDEMANDCAPACITY = ASGINFOObj.MixedInstancesPolicy.InstancesDistribution.OnDemandBaseCapacity
//     ONDEMANDRATIO = ASGINFOObj.MixedInstancesPolicy.InstancesDistribution.OnDemandPercentageAboveBaseCapacity

    ONDEMANDCAPACITY = 3
    ONDEMANDRATIO = 0
    OPTIONARGS = ""

    CALCULATEDMINSIZE = (MINSIZE * 2) * ((ONDEMANDCAPACITY / MINSIZE) + 1) + 1
    CALCULATEDMINSIZE = ((int) CALCULATEDMINSIZE * 100)/100
    CALCULATEDONDEMANDRATIO = (int)((ONDEMANDCAPACITY/MINSIZE) *100)

    env.CALCULATEDONDEMANDRATIO = "${CALCULATEDONDEMANDRATIO}"

    println "${CALCULATEDONDEMANDRATIO}"

    env.OPTIONARGS = "${OPTIONARGS}"


    sh '''#!/bin/bash
        set -x

        echo test $CALCULATEDONDEMANDRATIO

        echo $CALCULATEDONDEMANDRATIO
        echo $OPTIONARGS

        set +x
    '''
}

def test(){

    // ASGOUT=getASGINFO("t")
    // def list = ASGOUT.readLines()

    MIN=sh(returnStdout: true, script: 'echo test').trim()
    println "${MIN}"

}

def returnTest(){
    return readFile("/var/lib/jenkins/jsontmp")
}


def apply_chef(){

    sh '''#!/bin/bash
        set -x
        ./sun-ms-api-${ENVIRONMENT}_version = \"test\"
        set +x
    '''
}