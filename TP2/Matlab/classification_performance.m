function [accuracy] = classification_performance( expected_y, predicted_y, title_str, class_labels)
%CLASSIFICATION_ACCURACY Summary of this function goes here
%   True positive = correctly identified
%   False positive = incorrectly identified
%   True negative = correctly rejected
%   False negative = incorrectly rejected

population = zeros(1, length(unique(expected_y)));
accuracy = zeros(1, length(population));

% --------------------------------------------------------
% Calculate accuracy based on predicted and actual results

confusion_matrix = confusionmat(expected_y, predicted_y);

figure;
imagesc(confusion_matrix);
colorbar

ax = gca;        
title(sprintf('%s Confusion Matrix', title_str))
xlabel('Predicted Class')
ax.XTickLabel = class_labels;
ax.XTickLabelRotation = 90;
ax.XTick = 1:length(ax.XTickLabel);
ylabel('Expected Class')
ax.YTickLabel = class_labels;
ax.YTick = 1:length(ax.YTickLabel);

for idx = 1:length(confusion_matrix)
    
    true_positives = confusion_matrix(idx,idx);
    
    population(idx) = sum(confusion_matrix(idx,:)) + sum(confusion_matrix(:,idx)) - confusion_matrix(idx,idx);
    accuracy(idx) = true_positives / population(idx);
    
end

end
%EOF