const targetDir = '/user/job/';

describe('Creates user doc job images.', () => {

    describe('Creates Job-Overview images.', () => {
        it('Create job-overview.png and job-overview-tile.png', function () {
            cy.openJobOverview();

        });
    });

    describe('Creates Job-Editor images.', () => {
    });

});