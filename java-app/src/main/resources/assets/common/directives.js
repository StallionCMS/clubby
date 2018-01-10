

(function() {
    window.clubhouseLoadDirectives = function() {


        
        Vue.directive(
            'user-autocomplete',
            {
                inserted: function(el, binding) {
                    $(el).textcomplete([{
                        match: /(^|\s)@(\w*)$/,
                        template: function(user) {
                            return '<img style="max-width: 30px; max-height: 30px; display:inline-block;margin-right: 10px;" src="' + user.avatarUrl + '">' + user.displayName + ' (@' + user.username + ')';
                        },
                        search: function (term, callback) {
                            console.log('term! ', term);
                            term = term || '';
                            term = term.toLowerCase();
                            var users = ClubhouseVueApp.$store.state.allUsers.map(function(user) {
                                if (!term || user.username.toLowerCase().indexOf(term) > -1 || user.displayName.toLowerCase().indexOf(self.term) > -1) {
                                    return user;
                                } else {
                                    return null;
                                }
                            });
                            users = users.filter(function(user) { return user !== null;});
                            callback(users);
                            
                            //var words = ['google', 'facebook', 'github', 'microsoft', 'yahoo'];
                            //callback($.map(words, function (word) {
                            //    return word.indexOf(term) === 0 ? word : null;
                            //}));
                        },
                        replace: function (user) {
                            return '$1' + '@' + user.username + ' ';
                        }
                    }])
                        .on({
                            'textComplete:select': function (e, value, strategy) {

                            },
                            'textComplete:show': function (e) {
                                console.log('show text complete');
                                $(this).data('autocompleting', true);
                            },
                            'textComplete:hide': function (e) {
                                $(this).data('autocompleting', false);
                            }
                        })
                    ;                    
                }
            }
        );

        Vue.directive(
            'disable-for-processing',
            {
                inserted: function(el, binding) {
                    if (binding.value === true) {
                        el.classList.add("st-loading-mask");
                        el.setAttribute('disabled', 'true');
                    } else {
                        el.classList.remove("st-loading-mask");
                        el.removeAttribute('disabled');
                    }
                },
                update: function(el, binding) {
                    if (binding.value === true) {
                        el.classList.add("st-loading-mask");
                        el.setAttribute('disabled', 'true');
                    } else {
                        el.classList.remove("st-loading-mask");
                        el.removeAttribute('disabled');
                    }
                }
            }
        );
        
        Vue.directive(
            'raw-html',
            {
                inserted: function(el, binding) {
                    if (binding.value === null || binding.value === undefined) {
                        el.innerHTML = '';
                    } else {
                        el.innerHTML = binding.value;
                    }
                },
                update: function(el, binding) {
                    return;
                    if (binding.value === null  || binding.value === undefined) {
                        el.innerHTML = '';
                    } else {
                        el.innerHTML = binding.value;
                    }
                }
            }
        );
    };

}());
