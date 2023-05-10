let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity=0

localVideo.onplaying = () => {
    console.log("playing local video")
    localVideo.style.opacity=1
}

remoteVideo.onplaying = ()=>{
    console.log("playing remote video")

   remoteVideo.opacity.opacity=1;
}

let peer
function init(userId){
    peer = new Peer(userId,{
        host:'192.168.43.188'
        ,port:'2040'
        ,path:'/WebRtcVideoChat'
    })

    peer.on('open',()=>{
        // making kotlin function invokation
    })

    listen()
}


//listening to audio streams
function listen(){
    peer.on('call',(call)=>{
        navigator.getUserMedia({
            audio:true,
            video:true
        },(stream)=>{
            localVideo.srcObject = stream
            localStream = stream
            call.answer(stream)

            call.on('stream',(remoteStream)=>{
                remoteVideo.srcObject = remoteStream
                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"
            })

        })
    })
}

//logic of connecting the person we want to call
function startCall(otherUserId){

    navigator.getUserMedia({
        audio:true,
        video:true
    },(stream)=>{
        localVideo.srcObject = stream
        localStream = stream

        const call = peer.call(otherUserId,stream)

        call.on('stream',(remoteStream)=>{
            remoteVideo.srcObject = "primary-video"
            localVideo.className = "secondary-video"
        })
    })
}

function toggleAudio(b){
    if(b=="true"){
        localStream.getAudioTracks()[0].enabled = true
    }else{
        localStream.getAudioTracks()[0].enabled=false
    }
}

function toggleVideo(b){
    if(b=="true"){
        localStream.getVideoTracks()[0].enabled = true
    }else{
        localStream.getVideoTracks()[0].enabled = false
    }
}

