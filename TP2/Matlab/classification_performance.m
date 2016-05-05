function [accuracy] = classification_performance( expected_y, predicted_y )
%CLASSIFICATION_ACCURACY Summary of this function goes here
%   True positive = correctly identified
%   False positive = incorrectly identified
%   True negative = correctly rejected
%   False negative = incorrectly rejected

population = zeros(1, length(unique(predicted_y)));
accuracy = zeros(1, length(population));

% --------------------------------------------------------
% Calculate accuracy based on predicted and actual results

confusion_matrix = confusionmat(expected_y, predicted_y);

for idx = 1:length(confusion_matrix)
    
    true_positives = confusion_matrix(idx,idx);
    true_negatives = sum(confusion_matrix(:,idx)) - confusion_matrix(idx,idx);
    
    population(idx) = sum(confusion_matrix(idx,:)) + sum(confusion_matrix(:,idx)) - confusion_matrix(idx,idx);
    accuracy(idx) = (true_positives + true_negatives) / population(idx);
    
end

end
%EOF