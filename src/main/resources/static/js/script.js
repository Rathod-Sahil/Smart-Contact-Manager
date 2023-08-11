

const toggleSidebar=()=>{

    if($('.sidebar').is(":visible")){

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else{

        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
}

function deleteContact(cid){
    const swalWithBootstrapButtons = Swal.mixin({
customClass: {
  confirmButton: 'btn btn-success',
  cancelButton: 'btn btn-danger'
},
buttonsStyling: false
})

swalWithBootstrapButtons.fire({
title: 'Are you sure?',
text: "You won't be able to revert this!",
icon: 'warning',
showCancelButton: true,
confirmButtonText: 'Yes, delete it!',
cancelButtonText: 'No, cancel!',
reverseButtons: true
}).then((result) => {
if (result.isConfirmed) {
  swalWithBootstrapButtons.fire(
    window.location="/user/delete/" +cid,
    'Deleted!',
    'Your contact has been deleted.',
    'success'
  )
} else if (
  /* Read more about handling dismissals below */
  result.dismiss === Swal.DismissReason.cancel
) {
  swalWithBootstrapButtons.fire(
    'Cancelled',
    'Your contact is safe :)',
    'error'
  )
}
})
}

const search=()=>{
  let query=$("#search-input").val();

  if(query == "")
  {
    $(".search-result").hide();
  }else{

    //sending request to server

    let url=`http://localhost:8080/search/${query}`;

    fetch(url).then((response) => {
      return response.json();
    }).then((data) => {
      //data ......

      let text=`<div class='list-group'>`;

      data.forEach((contact) => {
        text+=`<a href="/user/${contact.cId}/contact" class='list-group-item list-group-item-action'>${contact.name}</a>`;
        
      });

      text += `</div>`;
      
      $(".search-result").html(text);
      $(".search-result").show();
    });

   
  }
}