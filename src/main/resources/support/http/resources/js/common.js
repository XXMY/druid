$.namespace("druid.common");

druid.common = function () {  
	var statViewOrderBy = '';
	var statViewOrderBy_old = '';
	var statViewOrderType = 'asc';
	var isOrderRequest = false;

	// only one page for now
	var sqlViewPage = 1;
	var sqlViewPerPageCount = 1000000;
	
	return  {
		init : function() {
			this.buildFooter();
			druid.lang.init();
		},

        getQueryParameter : function(paramName){
            var result = window.location.search.substr(1).match('(^|&)'+paramName+'=([^&]*)(&|$)');
            if (result != null) return decodeURIComponent(result[2]); return "";
        },

        buildRemoteUrl : function(source){
		    var remoteInfo = druid.common.getSelectRemoteInfo();
		    if('undefined' === typeof remoteInfo || remoteInfo === "")
		        return source;
		    if(source.includes('?')){
		        if(source.substring(source.length-1) === '&')
                    return source + "remote=" + druid.common.getSelectRemoteInfo();
		        else
                    return source + "&remote=" + druid.common.getSelectRemoteInfo();
            }

		    else
                return source + "?remote=" + druid.common.getSelectRemoteInfo();
        },

        remoteRedirect : function(source){
            var remoteName = druid.common.getSelectRemoteInfo();
            if(remoteName == null || remoteName === "")
                window.location.href=source;
            else
                window.location.href=source + "?remote=" + remoteName;
        },

        getSelectRemoteInfo : function(){
            var selected = $("#remoteInfoSelect").val();
            if('undefined' === typeof selected || '' === selected || null == selected)
                return druid.common.getQueryParameter("remote");

            return selected === "local" ? "" : selected;
        },

		buildHead : function(index) {
			$.get('header.html',function(html) {
				$(document.body).prepend(html);
				druid.lang.trigger();
				$(".navbar .nav li").eq(index).addClass("active");
			},"html");
						
		},
		
		buildFooter : function() {

			var html ='<footer class="footer">'+
					  '    		<div class="container">'+
					  '<a href="https://render.alipay.com/p/s/taobaonpm_click/druid_banner_click" target="new"><img src="https://render.alipay.com/p/s/taobaonpm_click/druid_banner"></a><br/>' +
				  	  '	powered by <a href="https://github.com/alibaba/" target="_blank">AlibabaTech</a> & <a href="http://www.sandzhang.com/" target="_blank">sandzhang</a> & <a href="http://melin.iteye.com/" target="_blank">melin</a> & <a href="https://github.com/shrekwang" target="_blank">shrek.wang</a>'+
				  	  '			</div>'+
					  ' </footer>';
			$(document.body).append(html);
		},
		
		ajaxRequestForReset : function() {
			if(!confirm("Are you sure to reset all stat? It'll clear all stat data !")){
				return;
			}
			
			$.ajax({
				type: 'POST',
				url: druid.common.buildRemoteUrl("reset-all.json"),
				success: function(data) {
					if (data.ResultCode == 1) {
						alert("already reset all stat");
					}
				},
				dataType: "json"
			});
		},

		ajaxRequestForLogAndReset : function() {
			if(!confirm("Are you sure to reset data source stat? It'll clear and log all stat data !")){
				return;
			}

			$.ajax({
				type: 'POST',
				url: druid.common.buildRemoteUrl("log-and-reset.json"),
				success: function(data) {
					if (data.ResultCode == 1) {
						alert("already reset all stat");
					}
				},
				dataType: "json"
			});
		},
		
		getAjaxUrl : function(uri) {
			var result = uri;

			if (statViewOrderBy != undefined)
				result += 'orderBy=' + statViewOrderBy + '&';

			if (statViewOrderType != undefined)
				result += 'orderType=' + statViewOrderType + '&';

			if (sqlViewPage != undefined)
				result += 'page=' + sqlViewPage + '&';

			if (sqlViewPerPageCount != undefined)
				result += 'perPageCount=' + sqlViewPerPageCount + '&';

			return result;
		},
		
		resetSortMark : function() {
			var divObj = document.getElementById('th-' + statViewOrderBy);
			var old_divObj = document.getElementById('th-' + statViewOrderBy_old);
			var replaceToStr = '';
			if (old_divObj) {
				var html = old_divObj.innerHTML;
				if (statViewOrderBy_old.indexOf('[') > 0)
					replaceToStr = '-';
				html = html.replace('▲', replaceToStr);
				html = html.replace('▼', replaceToStr);
				old_divObj.innerHTML = html
			}
			if (divObj) {
				var html = divObj.innerHTML;
				if (statViewOrderBy.indexOf('[') > 0)
					html = '';

				if (statViewOrderType == 'asc') {
					html += '▲';
				} else if (statViewOrderType == 'desc') {
					html += '▼';
				}
				divObj.innerHTML = html;
			}
      isOrderRequest = true;
			
			this.ajaxRequestForBasicInfo();
			return false;
		},

		setOrderBy : function(orderBy) {
			if (statViewOrderBy != orderBy) {
				statViewOrderBy_old = statViewOrderBy;
				statViewOrderBy = orderBy;
				statViewOrderType = 'desc';
				druid.common.resetSortMark();
				return;
			}

			statViewOrderBy_old = statViewOrderBy;

			if (statViewOrderType == 'asc')
				statViewOrderType = 'desc'
			else
				statViewOrderType = 'asc';

			druid.common.resetSortMark();
		},
		
		ajaxuri : "",
		handleCallback:null,
		handleAjaxResult : function(data) {
			druid.common.handleCallback(data);
      if (!isOrderRequest) {
        druid.lang.trigger();
      }
		},//ajax 处理函数
		ajaxRequestForBasicInfo : function() {
			$.ajax({
				type: 'POST',
				url: druid.common.buildRemoteUrl(druid.common.getAjaxUrl(druid.common.ajaxuri)),
				success: function(data) {
					druid.common.handleAjaxResult(data);
				},
				dataType: "json"
			});
		},
		
		subSqlString : function(sql, len) {
			if (sql == undefined || sql == null) {
				return '';
			}
			
			if (sql.length <= len)
				return sql;
			return sql.substr(0, len) + '...';
		},
		
		stripes : function() {
            $("#dataTable tbody tr").each(function () {
                $(this).removeClass("striped");
            });
            $("#dataTable tbody tr:even").each(function () {
                $(this).addClass("striped");
            });
        },
        
        getUrlVar : function(name) {
            var vars = {};
            var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
                vars[key] = value;
            });
        	return vars[name];
        }
	}
}();

$(document).ready(function() {
	druid.common.init();
});

function replace (data) {
	if((!data)||data === undefined){
		return '';
	}else{
		return format(data);
	}
}

function format(s) {
	var str=s+='';
	return str.replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
}
