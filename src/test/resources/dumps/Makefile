empty:
	./scripts/empty.sh > empty.dump

firstcommit:
	./scripts/firstcommit.sh > firstcommit.dump

svn_replace:
	./scripts/svn_replace.sh > svn_replace.dump

svn_rename:
	./scripts/svn_rename.sh > svn_rename.dump

svn_multi_file_delete:
	./scripts/svn_multi_file_delete.sh > svn_multi_file_delete.dump

simple_branch_and_merge:
	./scripts/simple_branch_and_merge.sh > simple_branch_and_merge.dump

simple_copy:
	./scripts/simple_copy.sh > simple_copy.dump
	./scripts/simple_copy2.sh > simple_copy2.dump
	echo "Further manual changes are required to update UUID and timestamps in simple_copy2.dump"

utf8_log_message:
	./scripts/utf8_log_message.sh > utf8_log_message.dump

add_and_multiple_change:
	./scripts/add_and_multiple_change.sh > add_and_multiple_change.dump

many_branches:
	./scripts/many_branches.sh > many_branches.dump
	sed 's/\/branch2/\/newbranchname/' many_branches.dump > many_branches_renamed.dump
	sed -i '600s/45/51/' many_branches_renamed.dump

svn_copy_file:
	./scripts/svn_copy_file.sh > svn_copy_file.dump
	# be aware of hand hacked time stamps to match svn_copy_file.dump
	./scripts/svn_copy_file_new_content.sh > svn_copy_file_new_content.dump

svn_copy_file_many_times:
	./scripts/svn_copy_file_many_times.sh > svn_copy_file_many_times.dump
	# be aware of hand hacked time stamps and uuid to match svn_copy_file_many_times.dump
	./scripts/svn_copy_file_many_times_new_content.sh > svn_copy_file_many_times_new_content.dump

svn_copy_and_delete:
	./scripts/svn_copy_and_delete.sh "this is some file content" > svn_copy_and_delete.before.dump
	# hand hacked time stamps to match the file above
	./scripts/svn_copy_and_delete.sh "i replaced the content" > svn_copy_and_delete.after.dump

add_and_copychange:
	./scripts/add_and_copychange.sh > add_and_copychange.dump

add_and_change_copy_delete:
	./scripts/add_and_change_copy_delete.sh > add_and_change_copy_delete.dump

set_root_property:
	./scripts/set_root_property.sh > ./set_root_property.dump
