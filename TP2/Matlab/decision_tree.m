% -----------------------
% train the decision tree

%{
% % cannot be done or else plotconfusion doesn't work
% get labels back for easier analysis
train_y = var_codes(train_y, end);
test_y = var_codes(test_y, end);
%}

tree = fitctree(train_X', train_y');
view(tree,'Mode','Graph');

% ----------------------
% test the decision tree

predicted_y = predict(tree, test_X');

% ---------------------------------
% analise performance of classifier

decision_tree_accuracy = classification_performance(test_y', predicted_y);

figure;
plot(decision_tree_accuracy);
title('Decision Tree Accuracy')
xlabel('Class')


%EOF