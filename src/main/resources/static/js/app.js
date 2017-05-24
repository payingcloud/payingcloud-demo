/**
 * Created by Lenovo on 2016/11/2.
 */
var app = angular.module('app', ['ngTouch', 'ui.grid', 'ui.grid.edit', 'ui.grid.selection', 'ui.grid.pagination', 'ui.grid.expandable']);

app.controller('MainCtrl', ['$scope', '$http', function ($scope, $http) {
    var b;
    var orderR;
    /*订单*/
    function orderList(page,size){
        $http.get('/order/list/charge?page='+page+'&size='+size)
            .success(function(data){
                $scope.order=data.content;
                $scope.pu=data.first;
                $scope.pd=data.last;
            }).error(function(data){
            alert(data.message);
        })
    }
    /*退款订单*/
    function orderRefund(page,pageSize,id){
        $http.get("/order/list/refund?chargeNo="+id+"&page="+page+"&size="+pageSize)
            .success(function(data){
                $scope.orderRefund = data.content;
                $scope.totalPagesR=data.totalPages;
                $scope.opu=data.first;
                $scope.opd=data.last;
            }).error(function (data) {
                alert(data.message);

            });
    }
    /*订单*/
    var p=0;
    orderList(p,10);
    $scope.upClick=function(){
        p--;
        orderList(p,$scope.t);
    };
    $scope.nextClick=function(){
        p++;
        orderList(p,$scope.t);
    };

    $scope.change=function(t){
        p=0;
        orderList(p,t);
    };

/*退款订单*/
    $scope.orderClick=function(){
        b=0;
        $scope.arr=this.o;
        $scope.s="10";
        orderR=this.o.chargeNo;
        $scope.ishow=true;
        orderRefund(b,10,orderR);

    };
    $scope.changeR=function(s){
        //console.log(orderR);
        b=0;
        orderRefund(b,s,orderR)
    };

    $scope.RUpClick=function(){
        b--;
        orderRefund(b,$scope.s,orderR)
    };

    $scope.RNextClick=function(){
        b++;
        orderRefund(b,$scope.s,orderR)
    };



    $scope.showMe = function (arr) {
        $scope.ishow=true;

        $scope.arr=arr;


    };

    $scope.cancel = function () {
        $scope.ishow = false;
    };

    $scope.refund = function () {
        $scope.tishi = true;
    };
    $scope.no = function () {
        $scope.tishi = false;
    };
    $scope.yes = function () {
        $http.get("/order/refund?chargeNo=" + this.arr.chargeNo)
            .success(function (data) {
                $scope.tishi = false;
                alert("退款成功");
                orderRefund(0,$scope.s,orderR);
                //orderRefund(0,$scope.s,orderR)

            }).error(function (data) {
            $scope.tishi = false;
            alert(data.message);
        });
    };

}]);
